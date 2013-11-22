package org.xwalk.runtime.extension.api.contacts;

import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactConstants {
    public static final String CUSTOM_MIMETYPE_LASTUPDATED =
            "vnd.android.cursor.item/contact_custom_lastupdated";
    public static final String CUSTOM_MIMETYPE_GENDER =
            "vnd.android.cursor.item/contact_custom_gender";

    public static final Map<String, String> findFieldMap =
            createStringMap(new String[]{
        "familyName", "familyNames",
        "givenName", "givenNames",
        "middleName", "middleNames",
        "additionalName", "additionalNames",
        "honorificPrefix", "honorificPrefixes",
        "honorificSuffix", "honorificSuffixes",
        "nickName", "nickNames",
        "email", "emails",
        "photo", "photos",
        "url", "urls",
        "phoneNumber", "phoneNumbers",
        "organization", "organizations",
        "jobTitle", "jobTitles",
        "note", "notes"
        // TODO(hdq): Should birthday and anniversary be distinguished?
    });

    public static final Map<String, Pair<String, String>> contactDataMap =
            createTripleMap(new String[]{
        "id", Data.CONTACT_ID, null,
        "displayName", StructuredName.DISPLAY_NAME, StructuredName.CONTENT_ITEM_TYPE,
        "familyNames", StructuredName.FAMILY_NAME, StructuredName.CONTENT_ITEM_TYPE,
        "givenNames", StructuredName.GIVEN_NAME, StructuredName.CONTENT_ITEM_TYPE,
        "middleNames", StructuredName.MIDDLE_NAME, StructuredName.CONTENT_ITEM_TYPE,
        "additionalNames",
                StructuredName.MIDDLE_NAME, StructuredName.CONTENT_ITEM_TYPE,
        "honorificPrefixes", StructuredName.PREFIX, StructuredName.CONTENT_ITEM_TYPE,
        "honorificSuffixes", StructuredName.SUFFIX, StructuredName.CONTENT_ITEM_TYPE,
        "nickNames", Nickname.NAME, Nickname.CONTENT_ITEM_TYPE,
        "categories",
                GroupMembership.GROUP_ROW_ID, GroupMembership.CONTENT_ITEM_TYPE,
        "gender", Data.DATA1, CUSTOM_MIMETYPE_GENDER,
        "lastUpdated", Data.DATA1, CUSTOM_MIMETYPE_LASTUPDATED,
        "birthday", Data.DATA1, Event.CONTENT_ITEM_TYPE,
        "anniversary", Data.DATA1, Event.CONTENT_ITEM_TYPE,
        "emails", Email.DATA, Email.CONTENT_ITEM_TYPE,
        "photos", Photo.PHOTO, Photo.CONTENT_ITEM_TYPE,
        "urls", Website.URL, Website.CONTENT_ITEM_TYPE,
        "phoneNumbers", Phone.NUMBER, Phone.CONTENT_ITEM_TYPE,
        "addresses", null, StructuredPostal.CONTENT_ITEM_TYPE,
        "streetAddress", StructuredPostal.STREET, StructuredPostal.CONTENT_ITEM_TYPE,
        "locality",
                StructuredPostal.NEIGHBORHOOD, StructuredPostal.CONTENT_ITEM_TYPE,
        "region", StructuredPostal.REGION, StructuredPostal.CONTENT_ITEM_TYPE,
        "postalCode", StructuredPostal.POSTCODE, StructuredPostal.CONTENT_ITEM_TYPE,
        "countryName", StructuredPostal.COUNTRY, StructuredPostal.CONTENT_ITEM_TYPE,
        "organizations", Organization.COMPANY, Organization.CONTENT_ITEM_TYPE,
        "jobTitles", Organization.TITLE, Organization.CONTENT_ITEM_TYPE,
        "notes", Note.NOTE, Note.CONTENT_ITEM_TYPE,
        "impp", Im.DATA, Im.CONTENT_ITEM_TYPE
    });

    public static Map<String, Pair<String, String>> createTripleMap(
            String[] triplets) {
        Map<String, Pair<String, String>> result =
                new HashMap<String, Pair<String, String>>();
        for (int i = 0; i < triplets.length; i += 3) {
            result.put(triplets[i],
                    new Pair<String, String>(triplets[i + 1], triplets[i + 2]));
        }
        return Collections.unmodifiableMap(result);
    }

    public static Map<String, String> createStringMap(String[] pairs) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < pairs.length; i += 2) {
          result.put(pairs[i], pairs[i + 1]);
        }
        return Collections.unmodifiableMap(result);
    }

    public static Map<String, String> createDataMap(String name) {
        return createStringMap(new String[]{"data", name});
    }

    public static Map<String, String> createValueMap(String name) {
        return createStringMap(new String[]{name, "value"});
    }

    public static final Map<String, String> photoDataMap =
            createDataMap(Photo.PHOTO);
    public static final Map<String, String> companyDataMap =
            createDataMap(Organization.COMPANY);
    public static final Map<String, String> jobtitleDataMap =
            createDataMap(Organization.TITLE);
    public static final Map<String, String> emailDataMap =
            createValueMap(Email.DATA);
    public static final Map<String, String> websiteDataMap =
            createValueMap(Website.DATA);
    public static final Map<String, String> phoneDataMap =
            createValueMap(Phone.DATA);
    public static final Map<String, String> noteDataMap =
            createValueMap(Note.NOTE);
    public static final Map<String, String> imDataMap = createValueMap(Im.DATA);
    public static final Map<String, String> addressDataMap =
            createStringMap(new String[]{
        StructuredPostal.STREET, "streetAddress",
        StructuredPostal.NEIGHBORHOOD, "locality",
        StructuredPostal.REGION, "region",
        StructuredPostal.POSTCODE, "postalCode",
        StructuredPostal.COUNTRY, "countryName"});

    @SuppressWarnings("serial")
    public static final Map<String, String> emailTypeMap =
            new HashMap<String, String>() { {
        put("type", Email.TYPE);
        put("isPrimary", Email.IS_PRIMARY);
        put("isSuperPrimary", Email.IS_SUPER_PRIMARY);
    } };

    @SuppressWarnings("serial")
    public static final Map<String, String> websiteTypeMap =
            new HashMap<String, String>() { {
        put("type", Website.TYPE);
        put("isPrimary", Website.IS_PRIMARY);
        put("isSuperPrimary", Website.IS_SUPER_PRIMARY);
    } };

    @SuppressWarnings("serial")
    public static final Map<String, String> addressTypeMap =
            new HashMap<String, String>() { {
        put("type", StructuredPostal.TYPE);
        put("isPrimary", StructuredPostal.IS_PRIMARY);
        put("isSuperPrimary", StructuredPostal.IS_SUPER_PRIMARY);
    } };

    @SuppressWarnings("serial")
    public static final Map<String, String> phoneTypeMap =
            new HashMap<String, String>() { {
        put("type", Phone.TYPE);
        put("isPrimary", Phone.IS_PRIMARY);
        put("isSuperPrimary", Phone.IS_SUPER_PRIMARY);
    } };

    @SuppressWarnings("serial")
    public static final Map<String, String> imTypeMap =
            new HashMap<String, String>() { {
        put("type", Im.TYPE);
        put("isPrimary", Im.IS_PRIMARY);
        put("isSuperPrimary", Im.IS_SUPER_PRIMARY);
    } };

    @SuppressWarnings("serial")
    public static final Map<String, Integer> emailTypeValuesMap =
            new HashMap<String, Integer>() { {
        put("work", Email.TYPE_WORK);
        put("home", Email.TYPE_HOME);
        put("mobile", Email.TYPE_MOBILE);
    } };

    @SuppressWarnings("serial")
    public static final Map<String, Integer> websiteTypeValuesMap =
            new HashMap<String, Integer>() { {
        put("blog", Website.TYPE_BLOG);
        put("ftp", Website.TYPE_FTP);
        put("home", Website.TYPE_HOME);
        put("homepage", Website.TYPE_HOMEPAGE);
        put("other", Website.TYPE_OTHER);
        put("profile", Website.TYPE_PROFILE);
        put("work", Website.TYPE_WORK);
    } };

    @SuppressWarnings("serial")
    public static final Map<String, Integer> addressTypeValuesMap =
            new HashMap<String, Integer>() { {
        put("work", StructuredPostal.TYPE_WORK);
        put("home", StructuredPostal.TYPE_HOME);
        put("other", StructuredPostal.TYPE_OTHER);
    } };

    @SuppressWarnings("serial")
    public static final Map<String, Integer> phoneTypeValuesMap =
            new HashMap<String, Integer>() { {
        put("home", Phone.TYPE_HOME);
        put("mobile", Phone.TYPE_MOBILE);
        put("work", Phone.TYPE_WORK);
        put("fax_work", Phone.TYPE_FAX_WORK);
        put("fax_home", Phone.TYPE_FAX_HOME);
        put("pager", Phone.TYPE_PAGER);
        put("other", Phone.TYPE_OTHER);
        put("callback", Phone.TYPE_CALLBACK);
        put("car", Phone.TYPE_CAR);
        put("company_main", Phone.TYPE_COMPANY_MAIN);
        put("isdn", Phone.TYPE_ISDN);
        put("main", Phone.TYPE_MAIN);
        put("other_fax", Phone.TYPE_OTHER_FAX);
        put("radio", Phone.TYPE_RADIO);
        put("telex", Phone.TYPE_TELEX);
        put("tty_tdd", Phone.TYPE_TTY_TDD);
        put("mobile", Phone.TYPE_WORK_MOBILE);
        put("work_pager", Phone.TYPE_WORK_PAGER);
        put("assistant", Phone.TYPE_ASSISTANT);
        put("mms", Phone.TYPE_MMS);
    } };

    @SuppressWarnings("serial")
    public static final Map<String, Integer> imTypeValuesMap =
            new HashMap<String, Integer>() { {
        put("work", Im.TYPE_WORK);
        put("home", Im.TYPE_HOME);
        put("other", Im.TYPE_OTHER);
    } };

    @SuppressWarnings("serial")
    public static final Map<String, Integer> imProtocolMap =
            new HashMap<String, Integer>() { {
        put("aim", Im.PROTOCOL_AIM);
        put("msn", Im.PROTOCOL_MSN);
        put("ymsgr", Im.PROTOCOL_YAHOO);
        put("skype", Im.PROTOCOL_SKYPE);
        put("qq", Im.PROTOCOL_QQ);
        put("gtalk", Im.PROTOCOL_GOOGLE_TALK);
        put("icq", Im.PROTOCOL_ICQ);
        put("jabber", Im.PROTOCOL_JABBER);
        put("netmeeting", Im.PROTOCOL_NETMEETING);
    } };

    public static final SimpleDateFormat jsonDateFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    java.util.Locale.getDefault());
    public static final SimpleDateFormat androidDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

    public static class ContactMap {
        public String name;
        public String mimeType;
        public Map<String, String> dataMap;
        public Map<String, String> typeMap;
        public Map<String, Integer> typeValueMap;
        public ContactMap(String n, Map<String, String> datas,
                  Map<String, String> types, Map<String, Integer> typeValues) {
            this.name = n;
            this.mimeType = contactDataMap.get(n).second;
            this.dataMap = datas;
            this.typeMap = types;
            this.typeValueMap = typeValues;
        }
    }

    @SuppressWarnings("serial")
    public static final List<ContactMap> contactMapList =
            new ArrayList<ContactMap>() { {
        add(new ContactMap("emails", emailDataMap,
                emailTypeMap, emailTypeValuesMap));
        add(new ContactMap("photos", photoDataMap, null, null));
        add(new ContactMap("urls", websiteDataMap,
                websiteTypeMap, websiteTypeValuesMap));
        add(new ContactMap("phoneNumbers", phoneDataMap,
                phoneTypeMap, phoneTypeValuesMap));
        add(new ContactMap("addresses", addressDataMap,
                addressTypeMap, addressTypeValuesMap));
        add(new ContactMap("organizations", companyDataMap, null, null));
        add(new ContactMap("jobTitles", jobtitleDataMap, null, null));
        add(new ContactMap("notes", noteDataMap, null, null));
        add(new ContactMap("impp", imDataMap, imTypeMap, imTypeValuesMap));
    } };
}
