package com.example.my_application_2;


import android.os.Bundle;
import android.util.Log;

import java.io.ByteArrayInputStream;
import com.igormaznitsa.jbbp.io.JBBPOut;
import com.igormaznitsa.jbbp.io.JBBPOut;
import com.igormaznitsa.jbbp.JBBPParser;
import com.igormaznitsa.jbbp.model.JBBPFieldStruct;
import com.igormaznitsa.jbbp.io.JBBPOut;
import com.igormaznitsa.jbbp.mapper.Bin;
import static com.igormaznitsa.jbbp.io.JBBPOut.*;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;


public class my_functions {

    public static int add(int VAR1, int VAR2) {
        return VAR1 + VAR2;
    }
    public static String getAnyData() {
        return "anyData";
    }

    // Функция подготовки массива байт (пакета) из топика для отправки в com port
    public static byte[] get_data_from_mosqito_as_bytes(String topic,String payload) {
        // Если есть дата в топике
        if(payload!="" && payload!="{}"){
            // Проверяем, что за топик
            if(topic == "/com/sunny/management/mouse/move"){return get_data_from_mosqito_mouse_move(topic, payload);}
            if(topic == "/com/sunny/management/mouse/click"){return get_data_from_mosqito_mouse_click(topic, payload);}

        }

        System.out.println("top="+topic+", data="+ payload);

        // в случае провала / не наш топик
        byte[] a={};
        return  a;
    }

    /*
    * Size of byte: 1 bytes.
    Size of short: 2 bytes.
    Size of int: 4 bytes.
    Size of long: 8 bytes.
    Size of char: 2 bytes.
    Size of float: 4 bytes.
    Size of double: 8 bytes.
    */

    // Функция перевода Топик в массив байт для (Переместить курсор виртуальной мыши нажатие)   com/sunny/management/mouse/move  {"button":"left","state":"free"}
    public static byte[] get_data_from_mosqito_mouse_click(String topic,String payload) {
        String button;
        // size of package for com port.   * Длина пакета без заголовка, длины и контрольной суммы (количество байт данных +1) – 1байт
        short l = 2;
        final byte [] data_array, arr_cont_summ;
        try {
            JSONObject data = new JSONObject(payload);
            button= (data.getString("button"));
            try {
                // Пакет без контрольной суммы
                if(button=="left")arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x04).Byte(0x01).End().toByteArray());
                else arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x04).Byte(0x02).End().toByteArray());
                // Весь пакет с КС ksum,   Подсчет контрольной суммы ksum(arr_cont_summ)
                data_array=concatenateByteArrays(arr_cont_summ, ksum(arr_cont_summ));
                //System.out.println("topic="+topic+", data_array_NEX="+ data_array);
                return data_array;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            System.out.println("topic="+topic+"- error");
            throw new RuntimeException(e);
        }
        byte[] a={};
        return  a;
    }

    // Функция перевода Топик в массив байт для (Переместить курсор виртуальной мыши перемещение)   com/sunny/management/mouse/move  {"x":"100","y":"50"}
    public static byte[] get_data_from_mosqito_mouse_move(String topic,String payload) {
        Short x,y;
        // size of package for com port.   * Длина пакета без заголовка, длины и контрольной суммы (количество байт данных +1) – 1байт
        short l = 5;
        final byte [] data_array, arr_cont_summ;
            try {
                JSONObject data = new JSONObject(payload);
                x= (short)(data.getInt("x"));
                y= (short)(data.getInt("y"));
                    try {
                        // Пакет без контрольной суммы
                        arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x03).Short(x).Short(y).End().toByteArray());
                        // Весь пакет с КС ksum,   Подсчет контрольной суммы ksum(arr_cont_summ)
                        data_array=concatenateByteArrays(arr_cont_summ, ksum(arr_cont_summ));
                        System.out.println("topic="+topic+", data_array_NEX="+ data_array);
                        return data_array;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            } catch (JSONException e) {
                System.out.println("topic="+topic+"- error");
                throw new RuntimeException(e);
            }
        byte[] a={};
        return  a;
    }

    // Подсчет контрольной суммы  CRC16CCITT.java     https://introcs.cs.princeton.edu/java/61data/CRC16CCITT.java
    public static byte[] ksum(byte [] array_of_bytes){
        int crc = 0xFFFF;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)

        // byte[] testBytes = "123456789".getBytes("ASCII");

        byte[] bytes = array_of_bytes;
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }

        crc &= 0xffff;

        try{
            System.out.println("CRC16-CCITT="+ crc+" CRC16-CCITT = " + Integer.toHexString(crc));
            return JBBPOut.BeginBin().Short(crc).End().toByteArray();  // ={0x01,0x01};
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] a={};
        return  a;
    }

    // Из 2х массивов байт делает один массив   С=А+В  https://stackoverflow.com/questions/5368704/appending-a-byte-to-the-end-of-another-byte     http://www.java2s.com/example/android-utility-method/array-add/add-byte-array-byte-element-cf8c7.html
    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}


