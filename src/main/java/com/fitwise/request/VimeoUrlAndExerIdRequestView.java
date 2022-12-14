package com.fitwise.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VimeoUrlAndExerIdRequestView {

    String vimeoUrl;
    long exerciseOrPromoId;
    boolean isPromotion=false;
}
