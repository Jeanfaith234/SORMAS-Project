package de.symeda.sormas.api;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.i18n.I18nProperties;

public enum Language {
	
	EN(new Locale("en"), "M/d/yyyy", "M/d/yyyy h:mm a", "M/d"),
	EN_NG(new Locale("en", "NG"), "M/d/yyyy", "M/d/yyyy h:mm a", "M/d"),
	EN_GH(new Locale("en", "GH"), "M/d/yyyy", "M/d/yyyy h:mm a", "M/d"),
	FR_FR(new Locale("fr", "FR"), "dd/MM/yyyy", "dd/MM/yyyy HH:mm", "dd/MM"),
	DE_DE(new Locale("de", "DE"), "dd.MM.yyyy", "dd.MM.yyyy HH:mm", "dd.MM"),
	ES_EC(new Locale("es", "EC"), "dd/MM/yyyy", "dd/MM/yyyy H:mm", "dd/MM"),
	FI_FI(new Locale("fi", "FI"), "M.d.yyyy", "M.d.yyyy H.mm", "M.d");

	/**
	 * Links locale strings to Languages 
	 */
	private static final Map<String, Language> languageLookup;
	static {
		languageLookup = new HashMap<>();
		for (Language lang : Language.values()) {
			Locale locale = lang.getLocale();
			languageLookup.put(createKey(locale.getLanguage(), locale.getCountry()), lang);
			
			//add first Language enum as default for the plain language Locale
			String langKey = createKey(locale.getLanguage(), "");
			if (! languageLookup.containsKey(langKey)) {
				languageLookup.put(langKey, lang);
			}
		}
	}
	
	private Locale locale;
	private String dateFormat;
	private String dateTimeFormat;
	private String dayMonthFormat;

	Language(Locale locale, String dateFormat, String dateTimeFormat, String dayMonthFormat) {
		this.locale = locale;
		this.dateFormat = dateFormat;
		this.dateTimeFormat = dateTimeFormat;
		this.dayMonthFormat = dayMonthFormat;
	}

	public String toString() {
		return I18nProperties.getEnumCaption(this);
	}

	public Locale getLocale() {
		return locale;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	public String getDayMonthFormat() {
		return dayMonthFormat;
	}

	/**
	 * @return EN when the locale does not fit any language
	 */
	public static Language fromLocaleString(String locale) {
		if (StringUtils.isBlank(locale)) {
			return EN;
		}
		
		String key = createKey(locale.trim().split("[_-]"));
		
		Language language = languageLookup.get(key);
		
		if (language == null) {
			LoggerFactory.getLogger(Language.class).warn("Unknown locale '{}'. Falling back to 'en'.", locale);
			language = EN;
		}
		
		return language;
	}

	private static String createKey(String ... localeComponents) {
		//only language and country are factored in
		StringBuilder sb = new StringBuilder(5);
		sb.append(localeComponents[0].toLowerCase());
		
		if (localeComponents.length > 1) {
			String c = localeComponents[1];
			if (StringUtils.isNotEmpty(c)) {
				sb.append('-').append(c.toUpperCase());
			}
		}
		return sb.toString();
	}
}
