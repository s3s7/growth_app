package com.sg.GrowthApp.controller;

import com.sg.GrowthApp.entity.GrowthRecord;
import com.sg.GrowthApp.form.GrowthRecordForm;
import com.sg.GrowthApp.service.GrowthRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/growth_records")
@RequiredArgsConstructor
public class GrowthRecordController {

    private final GrowthRecordService growthRecordService;

    @GetMapping
    public String index(@RequestParam(defaultValue = "date") String sort,
                        @RequestParam(defaultValue = "desc") String dir,
                        Model model) {
        model.addAttribute("growthRecords", growthRecordService.findAll(sort, dir));
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        return "growth_records/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("growthRecordForm", new GrowthRecordForm());
        return "growth_records/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute GrowthRecordForm growthRecordForm,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "growth_records/new";
        }
        growthRecordService.save(growthRecordForm);
        return "redirect:/growth_records/complete";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        model.addAttribute("growthRecord", growthRecordService.findById(id));
        return "growth_records/show";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        GrowthRecord record = growthRecordService.findById(id);
        GrowthRecordForm form = new GrowthRecordForm();
        form.setTitle(record.getTitle());
        form.setContent(record.getContent());
        form.setDate(record.getDate());
        form.setCategory(record.getCategory());
        form.setDurationSeconds(record.getDurationSeconds());
        model.addAttribute("growthRecordForm", form);
        model.addAttribute("recordId", id);
        return "growth_records/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute GrowthRecordForm growthRecordForm,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "growth_records/edit";
        }
        growthRecordService.update(id, growthRecordForm);
        return "redirect:/growth_records";
    }

    @PostMapping("/{id}/like")
    public String like(@PathVariable Long id,
                       @RequestParam(defaultValue = "date") String sort,
                       @RequestParam(defaultValue = "desc") String dir) {
        growthRecordService.like(id);
        return "redirect:/growth_records?sort=" + sort + "&dir=" + dir;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        growthRecordService.delete(id);
        return "redirect:/growth_records";
    }

    @GetMapping("/complete")
    public String complete() {
        return "growth_records/complete";
    }
}
**
        * 【機能の概要】
        * マーケティング担当者が設定したキャンペーン（販促施策）を、
        * ユーザーに向けて実際に配信（Deliver）するAPIです。
        *
        * 【仕様・ビジネスルール】
        * 1. 1回の配信につき、キャンペーンの予算を 1,000円 消費する。
        * 2. 予算が足りない（1,000円未満の）場合は配信できない。
        * 3. 配信実行の証跡として、必ず「配信履歴（DeliveryHistory）」をDBに残す。
        * 4. キャンペーン種別（EMAIL, SNS, AD）に応じて、対応する外部システムのAPIを呼び出して配信する。
        *
        * 【現在のEntity定義】
        * class Campaign {
 * private String id;
 * private String type; // "EMAIL", "SNS", "AD"
 * private int budget;
 * // ※現在は getter と setter のみが存在しています
         * }
 */

@RestController
@RequestMapping("/api/campaigns")
public class CampaignDeliveryController {
    @Autowired
    private DeliverCampaignUseCase deliverCampaignUseCase;

    @PostMapping("/{id}/deliveries")
    public ResponseEntity<String> deliver(@PathVariable String id) {
        try {
            deliverCampaignUseCase.execute(id);
            return ResponseEntity.status(200).body("ok");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("error: " + e.getMessage());
        }
    }
}

@Service
public class DeliverCampaignUseCase {
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private DeliveryHistoryRepository historyRepository;

    // 各種外部連携クライアント
    @Autowired private EmailClient emailClient;
    @Autowired private SnsClient snsClient;
    @Autowired private AdClient adClient;

    public void execute(String campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId);

        // 1. 予算チェックと計算
        if (campaign.getBudget() < 1000) {
            throw new IllegalArgumentException("予算が足りません");
        }
        campaign.setBudget(campaign.getBudget() - 1000);
        campaignRepository.save(campaign);

        // 2. 配信履歴の記録
        DeliveryHistory history = new DeliveryHistory(campaignId, LocalDateTime.now());
        historyRepository.save(history);

        // 3. 種別ごとの配信処理
        if (campaign.getType().equals("EMAIL")) {
            String emailBody = emailClient.buildTemplate(campaign.getTemplateId());
            emailClient.sendEmail(campaign.getTargetList(), emailBody);

        } else if (campaign.getType().equals("SNS")) {
            String snsText = campaign.getText() + " #campaign";
            snsClient.postTimeline(snsText, campaign.getImageUrl());

        } else if (campaign.getType().equals("AD")) {
            AdTarget target = adClient.createTargeting(campaign.getDemographic());
            adClient.submitAd(target, campaign.getBudgetLimit());

        } else {
            throw new RuntimeException("未対応の種別です");
        }
    }
}
/**
 * 【前提情報】
 * Campaign (キャンペーン): id, name
 * Lead (見込み客): id, campaignId, email
 */

@Service
public class GenerateCampaignReportUseCase {
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private LeadRepository leadRepository;

    public List<CampaignReportDTO> execute() {
        List<CampaignReportDTO> results = new ArrayList<>();

        // 1. 全キャンペーンを取得
        List<Campaign> campaigns = campaignRepository.findAll();

        for (Campaign campaign : campaigns) {
            // 2. キャンペーンごとにリードをDBから取得
            List<Lead> leads = leadRepository.findByCampaignId(campaign.getId());
            results.add(new CampaignReportDTO(campaign, leads));
        }

        return results;
    }
}