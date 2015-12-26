package org.mogara.sunny.timecapsule;

/**
 * This is a file created by sunny on 12/26/15 for TimeCapsule
 * Contact sunny via sunny@mogara.org for cooperation.
 */
public interface HttpCallbackListener {

    void onFinished(String response);

    void onError(Exception e);

}
