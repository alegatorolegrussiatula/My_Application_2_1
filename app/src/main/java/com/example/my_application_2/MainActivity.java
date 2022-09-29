package com.example.my_application_2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Scanner;
import java.io.*;

// библиотека парса / сбора массива данных
// https://jarcasting.ru/artifacts/com.igormaznitsa/jbbp/2.0.3/
// примеры
// https://qna.habr.com/q/510479
// https://raydac.blogspot.com/2014/08/java-binary-data-parsing-and-packing.html
/*
* https://github.com/raydac/java-binary-block-parser
* https://github.com/raydac/java-binary-block-parser/blob/master/README.md
* https://www.programcreek.com/java-api-examples/?api=com.igormaznitsa.jbbp.JBBPParser
*
**/

import static com.igormaznitsa.jbbp.io.JBBPOut.*;
import com.igormaznitsa.jbbp.exceptions.JBBPIOException;
import com.igormaznitsa.jbbp.io.JBBPOut;
import com.igormaznitsa.jbbp.mapper.Bin;
import com.igormaznitsa.jbbp.mapper.JBBPMapper;
import com.igormaznitsa.jbbp.model.JBBPFieldArrayUByte;
import com.igormaznitsa.jbbp.model.JBBPFieldShort;
import com.igormaznitsa.jbbp.model.JBBPFieldStruct;
import com.igormaznitsa.jbbp.utils.BinAnnotationWrapper;
import com.igormaznitsa.jbbp.utils.JBBPUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import com.igormaznitsa.jbbp.JBBPParser;
import com.igormaznitsa.jbbp.io.JBBPBitInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.Random;



public class MainActivity extends AppCompatActivity {

    //private  Button sbutton;
    private TextView book, chapter, verse;
    private boolean flag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //var_text = findViewById(R.id.id_text);
        //var_2 = findViewById(R.id.id_text);
        //TextView myTextView = (TextView)findViewById(R.id.id_text);


    }

    int i=0;
    public void onClick(View view) throws IOException {
        i++;
        //byte = 0XA0; //, 0XA2};
        /*   Пример взятия текста с текстового поля и записи его в другое поле
        TextView textView = findViewById(R.id.textView);
        EditText editText = findViewById(R.id.editText);
        textView.setText("Добро пожаловать, " + editText.getText());
        */
        book = (TextView) findViewById(R.id.id_text);
        chapter = (TextView) findViewById(R.id.id_text2);
        book.setText( "AbhiAndroid 444" ); //установить текст для текстового представления
        chapter.setText( "AbhiAndroid 777" ); //установить текст для текстового представления
        Log.i("myTag", "This is my message="+i);
        //System.out.print("7777777777777777");
        byte[] bytearray = "256".getBytes();
        int [] chunkSizes = new int[]{0x0D, 0x04, 0x06, 0x09, 0x07, 0x19, 0x0E5F, 0x00};
        //Log.i("byty", " bytearray="+ bytearray);

        Scanner in = new Scanner(System.in);
        //System.out.println("Input a number: ");

        final byte[] testData = new byte[] {4, (byte) 0x12, (byte) 0x34, 3, 5, 6, 7};
        //final GenAnnotations result = new GenAnnotations().read(new JBBPBitInputStream(new ByteArrayInputStream(testData)));

        final byte [] array =BeginBin().Bit(1, 2, 3, 0).Bit(true, false, true).Align().Byte(5).Short(1, 2, 3, 4, 5).Bool(true, false, true, true). Int(0xABCDEF23, 0xCAFEBABE).Long(0x123456789ABCDEF1L, 0x212356239091AB32L).End().toByteArray();
        Log.i("byty1=", " yty1="+  array);
        System.out.print("byty2="+array);

        //byte [] data = new byte [] {1,2,3,4,5,6,7,8,9}; //  , 0x19, 0x0F, 0x00
        byte [] data = new byte [] {(byte) 0x12, (byte) 0x34,}; //  , 0x19, 0x0F, 0x00
        JBBPFieldStruct parsed = JBBPParser.prepare("byte [2];").parse(new ByteArrayInputStream(data));
        //System.out.println("array = "+ Arrays.toString(parsed.findFieldForType(JBBPFieldArrayUByte.class).getArray()));
        System.out.println("=========================");

        final byte [] data_array = JBBPOut.BeginBin().Byte(0x12).Short(56).Byte(0x14).End().toByteArray();
        System.out.println("data_array="+data_array+", L="+data_array.length+ ", [0]="+data_array);
        if(data_array[0]==(byte) 0x12)
        {
            System.out.println("[3]="+data_array[3]);
            book.setText("");
        }

        //JBBPFieldStruct parsed_data_array = JBBPParser.prepare("byte;short;byte").parse(new ByteArrayInputStream(data));
        @Bin
        class Parsed { byte flag; short data;byte sum;}
        Parsed parsed_d = JBBPParser.prepare("byte flag; short data; byte sum;").parse(data_array).mapTo(new Parsed());
        System.out.println(" , data_array_out="+parsed_d.data);


        //JBBPFieldStruct parsed = JBBPParser.prepare("short[5];").parse(new ByteArrayInputStream(data));
        //JBBPFieldStruct parsed = JBBPParser.prepare("short; ubyte [5];").parse(new ByteArrayInputStream(data));
        //System.out.println("short = "+parsed.findFieldForType(JBBPFieldShort.class).getAsInt());
        //System.out.println("array = "+ Arrays.toString(parsed.findFieldForType(JBBPFieldArrayUByte.class).getArray()));
        //System.out.println("int="+sizeof(int))

        // Тест запаковки , координат мыши
        final byte [] data_array_1=my_functions.get_data_from_mosqito_as_bytes("/com/sunny/management/mouse/move","{'x':'100','y':'50'}");
        System.out.println("Topic1 NEX= "+data_array_1);   // {'x':'100','y':'50'}
        // Тест распаковки
        @Bin
        class Parsed_1 { byte flag; byte l; byte type; short x; short y;short  sum;}
        Parsed_1 parsed_d1 = JBBPParser.prepare("byte flag; byte l; byte type; short x; short y;short  sum;").parse(data_array_1).mapTo(new Parsed_1());
        System.out.println("Mouse x="+parsed_d1.x+", y="+parsed_d1.y+", summ="+parsed_d1.sum+", L="+parsed_d1.l+", flag="+(byte)data_array_1[0]+",type="+(byte)parsed_d1.flag);
        //if(data_array_1[2]==(byte)(0x03))Log.i("NEX", "flag="+data_array_1[0]);
        if((byte)parsed_d1.sum==(byte)(0xc8a8))Log.i("! NEX", "flag="+data_array_1[0]);


        // Тест запаковки , координат мыши
        final byte [] data_array_2=my_functions.get_data_from_mosqito_as_bytes("/com/sunny/execution/hardware/led","{'address':'10','brightness':'0','rgb':'#000000','duration':'0','function':'none'}");
        System.out.println("Topic2 NEX= "+data_array_2);   // {'x':'100','y':'50'}

    }
}