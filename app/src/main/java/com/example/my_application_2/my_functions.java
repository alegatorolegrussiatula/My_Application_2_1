/**
 *  1  Скопировать в проект файд my_functions.java
 *  2  Добавить в зависимости в файле build.gradle      implementation 'com.igormaznitsa:jbbp:2.0.2'   , (затем подгрузить  естейственно)
 *  3  В основной программе, в потоке, где "читаются" топики , их значения topic,payload,  вызвать метод: get_data_from_mosqito_as_bytes("topic","payload");
 *  4  Пример:
 *          // Код запаковки , координат мыши
 *         final byte [] data_array_1 = my_functions.get_data_from_mosqito_as_bytes("/com/sunny/management/mouse/move","{'x':'100','y':'50'}");
 *         System.out.println("Topic_1 NEX= "+data_array_1);
 *         // Остается, просто затем послать этот массив байт в com:
 *         // что-то вроде такого:
 *         com.write(data_array_1, len(data_array_1));
 *
 *   5 Библиотеки, которые могут понадобятся в основном фоновом приложении:
 * import android.os.Bundle;
 * import android.util.Log;
 * import java.util.Scanner;
 * import static com.igormaznitsa.jbbp.io.JBBPOut.*;
 * import com.igormaznitsa.jbbp.exceptions.JBBPIOException;
 * import com.igormaznitsa.jbbp.io.JBBPOut;
 * import com.igormaznitsa.jbbp.mapper.Bin;
 * import com.igormaznitsa.jbbp.model.JBBPFieldStruct;
 * import java.io.IOException;
 * import com.igormaznitsa.jbbp.JBBPParser;
 * import java.io.ByteArrayInputStream;
 */


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

    // Функция подготовки массива байт (пакета) из топика для отправки в com port
    public static byte[] get_data_from_mosqito_as_bytes(String topic,String payload) {
        // Если есть дата в топике
        if(payload!="" && payload!="{}"){
            // Проверяем, что за топик
            if(topic == "/com/sunny/management/mouse/move"){return get_data_from_mosqito_mouse_move(topic, payload);}
            if(topic == "/com/sunny/management/mouse/click"){return get_data_from_mosqito_mouse_click(topic, payload);}
            if(topic == "/com/sunny/management/mouse/unclick"){return get_data_from_mosqito_mouse_unclick(topic, payload);}
            if(topic == "/com/sunny/management/keyboard/button"){return get_data_from_mosqito_button_click(topic, payload);}
            if(topic == "/com/sunny/sensor/request/value"){return get_data_from_mosqito_sensor_getvalue(topic, payload);}
            if(topic == "/com/sunny/execution/hardware/laser"){return get_data_from_mosqito_laser(topic, payload);}
            if(topic == "/com/sunny/execution/hardware/led"){return get_data_from_mosqito_led(topic, payload);}
            if(topic == "/com/sunny/execution/hardware/turn"){return get_data_from_mosqito_turn(topic, payload);}
            System.out.println("topic="+topic+", data="+ payload);
        }



        // в случае провала / не наш топик
        byte[] a={0,0,0,0};
        return  a;
    }

    // Основные методы класса:

    /*
    * Size of byte: 1 bytes.
    Size of short: 2 bytes.
    Size of int: 4 bytes.
    Size of long: 8 bytes.
    Size of char: 2 bytes.
    Size of float: 4 bytes.
    Size of double: 8 bytes.
    */

    // Относительный поворот моторов tern  /com/sunny/execution/hardware/turn           (3 ways)
    // topic=="/com/sunny/execution/hardware/turn"   {"direction":"left","angle":"19.2","duration":"1.1","function":"linear"}
    public static byte[] get_data_from_mosqito_turn(String topic,String payload) {
        // Длинна пакета байт без заголовка, длины и контрольной суммы (количество байт данных +1) – 1байт
        short l = 6;
        // Пакет байт без контрольной суммы и с контрольной суммой
        final byte [] data_array, arr_cont_summ;

        // direction: (left/down/sw) ,  duration: fast (=11 ?!), angle: degrees
        String direction, duration,angle;

        // Настроки
        short motor = 0;         // Номер мотора: 1,2,3
        short steps =0;
        float alfa = (float) 1.8;
        short size_step=4;       // 4 / 32
        float koeff = size_step/alfa;
        short gerc= 10;          // Частота, Герцы
        short type_rotate = 1;   // 1- Относительный поворот,  0- Абсолютное позиционирование

        // Абсолютные значения паретров direction и duration
        short direction_value = 1;    // 1-left/down/sw ,  0 - right
        float duration_value;         // duration: fast (=11 ?!)
        float angle_value;

        // Парс даты с топика:
        try {
            JSONObject data = new JSONObject(payload);

            direction = (data.getString("direction"));
            duration = (data.getString("duration"));
            angle = (data.getString("angle"));

                duration_value = Float.parseFloat(duration);
                angle_value = Float.parseFloat(angle);

        }catch (JSONException e) {
            System.out.println("topic="+topic+"- error");
            throw new RuntimeException(e);
        }

        // Расчет параметров поворота перед запаковкой в байты:
        if(direction == "left" || direction == "right"){
            motor = 1;
            //n_steps_360 = size_step*alfa
            steps = (short) (Math.round(angle_value) * koeff);
            if (direction == "left")
                direction_value = 1;
            else
                direction_value = 0;
        }
        else if(direction == "up" || direction == "down"){
            motor = 2;
            //n_steps_360 = 32
            steps= (short) (Math.round(angle_value)*koeff);
            if (direction == "down")
                direction_value = 1;
            else
                direction_value = 0;
        }
        else if (direction == "cw" || direction == "ccw"){
            motor = 3;
            //n_steps_360 = 32
            steps= (short) (Math.round(angle_value)*koeff);
            if (direction == "ccw")
                direction_value = 1;
            else
                direction_value = 0;
        }
        else
            motor = 0;

        // Упаковка в байты
        try {
            // Пакет без контрольной суммы
            arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x21).Byte(motor).Short(steps).Byte(size_step).Byte(gerc).Byte(direction).Byte(type_rotate).End().toByteArray());

            // Весь пакет с КС ksum,   Подсчет контрольной суммы ksum(arr_cont_summ)
            data_array=concatenateByteArrays(arr_cont_summ, ksum(arr_cont_summ));
            return data_array;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Возвращаем пустой массив, если что-то пошло не так
        byte[] a=  null;
        return  a;
    }

    // Включение лампочки   com/sunny/execution/hardware/led      {"address":"10","brightness":"0","rgb":"#000000","duration":"0","function":"none"}
    public static byte[] get_data_from_mosqito_led(String topic,String payload) {
        String fingerprint;
        byte brightness;
        short address;
        // size of package for com port.   * Длина пакета без заголовка, длины и контрольной суммы (количество байт данных +1) – 1байт
        short l = 4;
        // Пакет байт без контрольной суммы и с контрольной суммой
        final byte [] data_array, arr_cont_summ;

        try {
            // Парс даты с топика:
            JSONObject data = new JSONObject(payload);
            address= (short)(data.getInt("address"));
            brightness= (byte)(data.getInt("brightness"));

            try {
                // Пакет без контрольной суммы
                arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x33).Short(address).Byte(brightness).End().toByteArray());

                // Весь пакет с КС ksum,   Подсчет контрольной суммы ksum(arr_cont_summ)
                data_array=concatenateByteArrays(arr_cont_summ, ksum(arr_cont_summ));
                System.out.println("Topic="+topic+"- ok");
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


    // Включение  Лазера com/sunny/execution/hardware/laser    {"laser_on":"0"}
    public static byte[] get_data_from_mosqito_laser(String topic,String payload) {
        String laser_on,fingerprint;
        byte code=0;
        // size of package for com port.   * Длина пакета без заголовка, длины и контрольной суммы (количество байт данных +1) – 1байт
        short l = 2;
        final byte [] data_array, arr_cont_summ;

        try {
            // Парс даты с топика:
            JSONObject data = new JSONObject(payload);
            laser_on= (data.getString("laser_on"));

            try {
                // Пакет без контрольной суммы
                if(laser_on=="1") code=1;
                arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x31).Byte(code).End().toByteArray());

                // Весь пакет с КС ksum,   Подсчет контрольной суммы ksum(arr_cont_summ)
                data_array=concatenateByteArrays(arr_cont_summ, ksum(arr_cont_summ));
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


    // Запрос состояния сенсора  is waiting= 0x12 anser   com/sunny/sensor/request/value      2-байта-номер датчика
    public static byte[] get_data_from_mosqito_sensor_getvalue(String topic,String payload) {
        String key,fingerprint;
        short number_sensor=0;
        // size of package for com port.   * Длина пакета без заголовка, длины и контрольной суммы (количество байт данных +1) – 1байт
        short l = 3;
        final byte [] data_array, arr_cont_summ;

        try {
            // Парс даты с топика:
            JSONObject data = new JSONObject(payload);
            //code= (short)(data.getInt("code"));
            key= (data.getString("key"));
            // 2 байта - номер датчика  пакете для com порта, поэтому имя каждого татчика из payload от топика, мы присваем номер сенсора
            // 0 - лазер, 1- мотор1, 2- мотор2, 3- мотор3
            try {
                // Пакет без контрольной суммы
                if(key == "laser_on") number_sensor=0;
                if(key == "verticalAxis")number_sensor=1;  // мотор 1
                if(key == "horizonAxis")number_sensor=2;   // мотор 2
                if(key == "Axis")number_sensor=3;          // мотор 3

                arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x11).Short(number_sensor).End().toByteArray());

                // Весь пакет с КС ksum,   Подсчет контрольной суммы ksum(arr_cont_summ)
                data_array=concatenateByteArrays(arr_cont_summ, ksum(arr_cont_summ));
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

    // Отправить нажатие клавиши клавиатуры   com/sunny/management/keyboard/button   {"code":"38","state":"free"}
    public static byte[] get_data_from_mosqito_button_click(String topic,String payload) {
        String state;
        short code;
        // size of package for com port.   * Длина пакета без заголовка, длины и контрольной суммы (количество байт данных +1) – 1байт
        short l = 3;
        final byte [] data_array, arr_cont_summ;

        try {
            // Парс даты с топика:
            JSONObject data = new JSONObject(payload);
            code= (short)(data.getInt("code"));
            state= (data.getString("state"));
            try {
                // Пакет без контрольной суммы
                arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x01).Short(code).End().toByteArray());

                // Весь пакет с КС ksum,   Подсчет контрольной суммы ksum(arr_cont_summ)
                data_array=concatenateByteArrays(arr_cont_summ, ksum(arr_cont_summ));
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


    // Функция перевода Топик в массив байт для ( отпускание кнопки  мыши нажатие)   com/sunny/management/mouse/unclick  {"button":"left","state":"free"}
    public static byte[] get_data_from_mosqito_mouse_unclick(String topic,String payload) {
        String button;
        // size of package for com port.   * Длина пакета без заголовка, длины и контрольной суммы (количество байт данных +1) – 1байт
        short l = 2;
        final byte [] data_array, arr_cont_summ;

        try {
            // Парс даты с топика:
            JSONObject data = new JSONObject(payload);
            button= (data.getString("button"));
            try {
                // Пакет без контрольной суммы
                if(button=="left")arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x05).Byte(0x01).End().toByteArray());
                else if(button=="right")arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x05).Byte(0x03).End().toByteArray());
                else arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x05).Byte(0x02).End().toByteArray());

                // Весь пакет с КС ksum,   Подсчет контрольной суммы ksum(arr_cont_summ)
                data_array=concatenateByteArrays(arr_cont_summ, ksum(arr_cont_summ));

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

    // Функция перевода Топик в массив байт для (Нажатие кнопки  мыши нажатие)   com/sunny/management/mouse/click  {"button":"left","state":"free"}
    public static byte[] get_data_from_mosqito_mouse_click(String topic,String payload) {
        String button;
        // size of package for com port.   * Длина пакета без заголовка, длины и контрольной суммы (количество байт данных +1) – 1байт
        short l = 2;
        final byte [] data_array, arr_cont_summ;

        try {
            // Парс даты с топика:
            JSONObject data = new JSONObject(payload);
            button= (data.getString("button"));
            try {
                // Пакет без контрольной суммы
                if(button=="left")arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x04).Byte(0x01).End().toByteArray());
                else if(button=="right")arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x04).Byte(0x03).End().toByteArray());
                else arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x04).Byte(0x02).End().toByteArray());

                // Весь пакет с КС ksum,   Подсчет контрольной суммы ksum(arr_cont_summ)
                data_array=concatenateByteArrays(arr_cont_summ, ksum(arr_cont_summ));
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
                // Парс даты с топика:
                JSONObject data = new JSONObject(payload);
                x= (short)(data.getInt("x"));
                y= (short)(data.getInt("y"));
                    try {
                        // Пакет без контрольной суммы
                        arr_cont_summ=(JBBPOut.BeginBin().Byte(0xA0).Byte(l).Byte(0x03).Short(x).Short(y).End().toByteArray());

                        // Весь пакет с КС ksum,   Подсчет контрольной суммы ksum(arr_cont_summ)
                        data_array=concatenateByteArrays(arr_cont_summ, ksum(arr_cont_summ));
                        //System.out.println("topic="+topic+", data_array_NEX="+ data_array);
                        System.out.println("Topic="+topic+"- ok");
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

    // Подсчет контрольной суммы  CRC16CCITT.java
    // https://introcs.cs.princeton.edu/java/61data/CRC16CCITT.java
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

    // Из 2х массивов байт делает один массив   С=А+В
    // https://stackoverflow.com/questions/5368704/appending-a-byte-to-the-end-of-another-byte
    // http://www.java2s.com/example/android-utility-method/array-add/add-byte-array-byte-element-cf8c7.html
    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}


