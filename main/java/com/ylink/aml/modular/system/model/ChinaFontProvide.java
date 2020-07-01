package com.ylink.aml.modular.system.model;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontProvider;
import com.itextpdf.text.pdf.BaseFont;

public  final class ChinaFontProvide implements FontProvider
{

    @Override public boolean isRegistered(String s)
    {
        return false;
    }

    @Override public Font getFont(String arg0, String arg1, boolean arg2, float arg3, int arg4, BaseColor arg5)
    {
        BaseFont bfChinese = null;
        try
        {
            bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        Font FontChinese = new Font(bfChinese, 12, Font.NORMAL);
        return FontChinese;
    }
}
