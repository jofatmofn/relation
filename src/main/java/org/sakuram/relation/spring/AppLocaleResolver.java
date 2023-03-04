package org.sakuram.relation.spring;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.sakuram.relation.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.LocaleResolver;

public class AppLocaleResolver implements LocaleResolver {
	
	@Autowired
	HttpSession httpSession;
	
	@Override
	public Locale resolveLocale(HttpServletRequest request) {
    	Locale locale;
    	Long currentLanguageDvId;
    	
    	currentLanguageDvId = httpSession == null || httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_LANGUAGE_DV_ID) == null ? null :
    			(long)(httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_LANGUAGE_DV_ID));
		if (currentLanguageDvId == null) {
			locale = Locale.ENGLISH;	// TODO: To come from configuration / Constants.
		} else if (currentLanguageDvId.equals(Constants.ENGLISH_LANGUAGE_DV_ID)) {
			locale = Locale.ENGLISH;
		} else if (currentLanguageDvId.equals(Constants.TAMIL_LANGUAGE_DV_ID)) {
			locale = new Locale("ta", "IN");	// TODO: To come from DomainValue table?
		} else {
			locale = Locale.ENGLISH;	// TODO: To come from configuration / Constants.
		}
		return locale;
	}

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        // Nothing
    }
}
