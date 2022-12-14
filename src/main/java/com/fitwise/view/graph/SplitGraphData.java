package com.fitwise.view.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Created by Vignesh G on 13/03/20
 */

@Getter
@Setter
public class SplitGraphData {

    private int totalCount;

    private List<SplitGraphEntry> graphEntryList;

}
