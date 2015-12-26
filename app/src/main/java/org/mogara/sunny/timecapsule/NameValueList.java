package org.mogara.sunny.timecapsule;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a file created by sunny on 12/27/15 for TimeCapsule
 * Contact sunny via sunny@mogara.org for cooperation.
 */
public class NameValueList {

    private List<NameValuePair> list = new ArrayList<NameValuePair>();

    private boolean encoded = false;

    public List<NameValuePair> getList() {
        return list;
    }

    public void addPair(final String name, final String value) {
        list.add(new BasicNameValuePair(name, value));
    }

    public boolean isEncoded() {
        return encoded;
    }

    public void setEncoded(final boolean encoded) {
        this.encoded = encoded;
    }
}
