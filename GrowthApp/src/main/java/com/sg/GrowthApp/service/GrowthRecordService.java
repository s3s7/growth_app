package com.sg.GrowthApp.service;

import com.sg.GrowthApp.entity.GrowthRecord;
import com.sg.GrowthApp.form.GrowthRecordForm;
import com.sg.GrowthApp.repository.GrowthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrowthRecordService {

    private final GrowthRecordRepository growthRecordRepository;

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("date", "title", "category", "createdAt");

    public List<GrowthRecord> findAll() {
        return growthRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "date"));
    }

    public List<GrowthRecord> findAll(String sortField, String direction) {
        String field = ALLOWED_SORT_FIELDS.contains(sortField) ? sortField : "date";
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return growthRecordRepository.findAll(Sort.by(dir, field));
    }

    public GrowthRecord findById(Long id) {
        return growthRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("記録が見つかりません: " + id));
    }

    public GrowthRecord save(GrowthRecordForm form) {
        GrowthRecord record = new GrowthRecord();
        record.setTitle(form.getTitle());
        record.setContent(form.getContent());
        record.setDate(form.getDate());
        record.setCategory(form.getCategory());
        record.setDurationSeconds(form.getDurationSeconds());
        return growthRecordRepository.save(record);
    }

    public void like(Long id) {
        GrowthRecord record = findById(id);
        record.setLikeCount(record.getLikeCount() + 1);
        growthRecordRepository.save(record);
    }

    public void delete(Long id) {
        growthRecordRepository.deleteById(id);
    }

    public GrowthRecord update(Long id, GrowthRecordForm form) {
        GrowthRecord existing = findById(id);
        existing.setTitle(form.getTitle());
        existing.setContent(form.getContent());
        existing.setDate(form.getDate());
        existing.setCategory(form.getCategory());
        existing.setDurationSeconds(form.getDurationSeconds());
        return growthRecordRepository.save(existing);
    }
}
