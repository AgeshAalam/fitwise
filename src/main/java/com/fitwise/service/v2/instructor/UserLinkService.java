package com.fitwise.service.v2.instructor;

import com.fitwise.entity.User;
import com.fitwise.entity.user.UserLinkExternal;
import com.fitwise.entity.user.UserLinkSocial;
import com.fitwise.repository.user.UserLinkExternalRepository;
import com.fitwise.repository.user.UserLinkSocialRepository;
import com.fitwise.view.user.ExternalLinkView;
import com.fitwise.view.user.SocialLinkView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLinkService {

    private final UserLinkSocialRepository userLinkSocialRepository;
    private final UserLinkExternalRepository userLinkExternalRepository;

    /**
     * Get all user social links
     * @param user User to get social link
     * @return Social links view list
     */
    public List<SocialLinkView> getSocialLinks(final User user){
        List<UserLinkSocial> userLinkSocials = userLinkSocialRepository.findByUserProfileUser(user);
        List<SocialLinkView> socialLinkViews = new ArrayList<>();
        for (UserLinkSocial userLinkSocial : userLinkSocials) {
            if(userLinkSocial.getLink() == null){
                continue;
            }
            SocialLinkView socialLinkView = new SocialLinkView();
            socialLinkView.setSocialMediaId(userLinkSocial.getSocialMedia().getSocialMediaId());
            socialLinkView.setImageUrl(userLinkSocial.getSocialMedia().getSocialMediaImage().getImagePath());
            socialLinkView.setLink(userLinkSocial.getLink());
            socialLinkViews.add(socialLinkView);
        }
        return socialLinkViews;
    }

    /**
     * Get all user external links
     * @return External link view list
     */
    public List<ExternalLinkView> getExternalLinks(final User user) {
        List<UserLinkExternal> userExternalLinks = userLinkExternalRepository.findByUserProfileUser(user);
        List<ExternalLinkView> externalLinkViews = new ArrayList<>();
        for (UserLinkExternal userLinkExternal : userExternalLinks) {
            ExternalLinkView externalLinkView = new ExternalLinkView();
            externalLinkView.setLinkId(userLinkExternal.getLinkId());
            externalLinkView.setName(userLinkExternal.getName());
            externalLinkView.setLink(userLinkExternal.getLink());
            externalLinkView.setImageId(userLinkExternal.getImage().getImageId());
            externalLinkView.setImageUrl(userLinkExternal.getImage().getImagePath());
            externalLinkViews.add(externalLinkView);
        }
        return externalLinkViews;
    }

}
