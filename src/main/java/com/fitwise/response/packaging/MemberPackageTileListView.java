package com.fitwise.response.packaging;

import lombok.Data;

import java.util.List;

@Data
public class MemberPackageTileListView {

    private List<MemberPackageTileView> packages;

    private int totalCount;


}
