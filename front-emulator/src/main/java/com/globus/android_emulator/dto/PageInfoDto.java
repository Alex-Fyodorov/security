package com.globus.android_emulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageInfoDto {
    private int number;
    private int size;
    private int totalElements;
    private int totalPages;
}
