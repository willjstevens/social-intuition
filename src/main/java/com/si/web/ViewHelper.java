/*
 * Property of Will Stevens
 * All rights reserved.
 */
package com.si.web;

import com.si.Category;
import com.si.UrlBuilder;
import com.si.UrlConstants;
import com.si.entity.Intuition;
import com.si.entity.StoredImageInfo;
import com.si.log.LogManager;
import com.si.log.Logger;
import com.si.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wstevens
 */
@Component
public class ViewHelper
{
    private static final Logger logger = LogManager.manager().newLogger(ViewHelper.class, Category.CONTROLLER);
    @Autowired private ConfigurationService configurationService;

    /*
     * See FB documentation at
     *   - https://developers.facebook.com/docs/plugins/share-button
     *   - https://developers.facebook.com/docs/sharing/webmasters
     */
    public String buildFacebookSharePageObject(Intuition intuition, String appId) {
        // prepare info
        String intuitionUrl = buildIntuitionUrl(intuition);
        // build html
        StringBuilder builder = new StringBuilder();
        builder.append("<html>\n");
        builder.append("<head>\n");
        builder.append("\t<title>Social Intuition</title>\n");
        builder.append("\t<meta property=\"og:title\"           content=\"").append("SOCIAL INTUITION").append("\" />\n");
        builder.append("\t<meta property=\"og:url\"             content=\"").append(intuitionUrl).append("\" />\n");
        builder.append("\t<meta property=\"og:type\"            content=\"").append("website").append("\" />\n");
        builder.append("\t<meta property=\"og:description\"     content=\"").append(intuition.getIntuitionText()).append("\" />\n");
        builder.append("\t<meta property=\"fb:app_id\"          content=\"").append(appId).append("\" />\n");
        builder.append("\t<meta property=\"og:site_name\"       content=\"").append("Social Intuition").append("\" />\n");

        List<StoredImageInfo> imageInfos = intuition.getImageInfos();
        if (imageInfos != null && !imageInfos.isEmpty()) {
            String imageUrl = imageInfos.get(0).getSecureUrl();
            builder.append("\t<meta property=\"og:image\"           content=\"").append(imageUrl).append("\" />\n");
        }

        builder.append("</head>\n");
        builder.append("<body>\n");
        builder.append("\t<h3>\"").append(intuition.getIntuitionText()).append("\"</h3>\n");
        builder.append("\t<p> - \"").append(intuition.getUser().getFullName()).append("\"</p>\n");
        builder.append("</body>\n");
        builder.append("</html>\n");
        String html = builder.toString();
        if (logger.isFine()) {
            logger.fine("Returning the following Facebook Share OG page HTML: \n" + html);
        }
        return html;
    }

    /*
     * See FB documentation at
     *   - https://dev.twitter.com/cards/overview
     *   - https://dev.twitter.com/cards/types/summary
     */
    public String buildTwitterTweetCardObject(Intuition intuition) {
        // prepare info
        String intuitionUrl = buildIntuitionUrl(intuition);

        // build html
        StringBuilder builder = new StringBuilder();
        builder.append("<html>\n");
        builder.append("<head>\n");
        builder.append("\t<title>Social Intuition</title>\n");
        builder.append("\t<meta name=\"twitter:card\"        content=\"").append("summary").append("\" />\n");
        builder.append("\t<meta name=\"twitter:site\"        content=\"").append("@socintuition").append("\" />\n");
        builder.append("\t<meta name=\"twitter:title\"       content=\"").append(intuition.getIntuitionText()).append("\" />\n");
        builder.append("\t<meta name=\"twitter:description\" content=\"").append("View it on SOCIAL INTUITION").append("\" />\n");

        List<StoredImageInfo> imageInfos = intuition.getImageInfos();
        if (imageInfos != null && !imageInfos.isEmpty()) {
            String imageUrl = imageInfos.get(0).getSecureUrl();
            builder.append("\t<meta name=\"twitter:image\"       content=\"").append(imageUrl).append("\" />\n");
        }
        builder.append("</head>\n");
        builder.append("<body>\n");
        builder.append("\t<h3>\"").append(intuition.getIntuitionText()).append("\"</h3>\n");
        builder.append("\t<p> - \"").append(intuition.getUser().getFullName()).append("\"</p>\n");
        builder.append("</body>\n");
        builder.append("</html>\n");
        String html = builder.toString();
        if (logger.isFine()) {
            logger.fine("Returning the following Twitter tweet page HTML: \n" + html);
        }
        return html;
    }

    private String buildIntuitionUrl(Intuition intuition) {
        UrlBuilder intuitionBuilder = new UrlBuilder(configurationService);
        intuitionBuilder.setDomainPrefixSecure();
        intuitionBuilder.appendActionPathPart(UrlConstants.INTUITION);
        intuitionBuilder.appendActionPathPart(intuition.getId());
        return intuitionBuilder.buildUrl();
    }
}
