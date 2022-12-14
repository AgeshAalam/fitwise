package com.fitwise.program.model;

import com.fitwise.entity.Promotions;
import com.fitwise.exercise.model.VimeoModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromoUploadResponseModel {

    private VimeoModel vimeoModel;
    private Promotions promotions;
}
