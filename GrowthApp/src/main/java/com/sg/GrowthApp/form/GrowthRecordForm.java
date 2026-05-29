package com.sg.GrowthApp.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GrowthRecordForm {

    @NotBlank(message = "タイトルを入力してください")
    private String title;

    @NotBlank(message = "内容を入力してください")
    private String content;

    @NotNull(message = "日付を入力してください")
    private LocalDate date;

    private String category;

    private Long durationSeconds;
}
