package com.fitwise.view.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminUserListResponseView {

    private Long totalCount;

    private List<AdminUserTileResponseView> adminUsers;
}
