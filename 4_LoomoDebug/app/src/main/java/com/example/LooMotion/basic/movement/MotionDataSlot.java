package com.example.LooMotion.basic.movement;

import android.util.Log;

public class MotionDataSlot {
    final String TAG = "DataSlot";

    private float[] data;
    private int length;

    /**
     * Inital the dataslot
     *
     * @param num
     */
    public MotionDataSlot(int num) {
        data = new float[num];
        length = num;
    }

    public int getLength() {
        return this.length;
    }

    /**
     * Push a data in to slot
     * Delete the earliest data
     *
     * @param num
     */
    public void push(float num) {
        for (int i = data.length - 1; i > 0; i--) {
            data[i] = data[i - 1];
        }
        data[0] = num;
    }

    /**
     * Get average of data
     */
    public float getAvg(){
        float sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i];
        }

        return sum / data.length;
    }

    /**
     * Get the sum of several new incomming data
     * The num of data is according to the input param
     * @param num
     * @return
     */
    public float getUpdateSum(int num){
        float sum = 0;
        for(int i = length - 1; i >= length - num; i--){
            sum += data[i];
        }
        return  sum;
    }

    /**
     * Get variance of all data
     * @return
     */
    public float getVariance(){
        float avg = getAvg();
        float sum = 0;
        for (int i = 0; i < data.length; i++) {
            float delt = data[i] - avg;
            sum += delt * delt;
        }
            return sum / length;
    }

    /**
     * Get latest data from slot
     *
     * @return
     */
    public float top() {
        return data[0];
    }

    /**
     * Get the data in time sequence
     * Return the latest data when index is 0
     * @param index
     * @return
     */
    public float get(int index){
        return data[index];
    }

    /**
     *  print slot in Log file
     */
    public void printLog(){
        for(int i = 0 ; i < length; i++){
            Log.d(TAG, "Slot " + i + " : " + data[i]);
        }
    }
}
