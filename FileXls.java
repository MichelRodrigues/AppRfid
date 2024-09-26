package com.UHF.scanlable;

import android.util.Xml;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;
import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.xmlpull.v1.XmlPullParser;

/* loaded from: classes.dex */
public class FileXls {
    public static final int ADD_DATA_FAIL = -2;
    public static final int CREATE_FAIL = -1;
    private static final int DEFAULT_SHEET = 0;

    public static String readXLS(String path) {
        String str;
        String str2 = android.device.sdk.BuildConfig.FLAVOR;
        try {
            Workbook workbook = Workbook.getWorkbook(new File(path));
            Sheet sheet = workbook.getSheet(0);
            int columns = sheet.getColumns();
            int rows = sheet.getRows();
            String str3 = android.device.sdk.BuildConfig.FLAVOR;
            for (int i = 0; i < rows; i++) {
                for (int i2 = 0; i2 < columns; i2++) {
                    try {
                        Cell cell = sheet.getCell(i2, i);
                        if (cell.getType() == CellType.NUMBER) {
                            str = ((NumberCell) cell).getValue() + android.device.sdk.BuildConfig.FLAVOR;
                        } else if (cell.getType() == CellType.DATE) {
                            str = android.device.sdk.BuildConfig.FLAVOR + ((DateCell) cell).getDate();
                        } else {
                            str = android.device.sdk.BuildConfig.FLAVOR + cell.getContents();
                        }
                        str3 = str3 + "  " + str;
                    } catch (Exception unused) {
                        str2 = str3;
                        return str2;
                    }
                }
                str3 = str3 + "\n";
            }
            workbook.close();
            return str3;
        } catch (Exception unused2) {
        }
    }

    public static ArrayList<HashMap<String, Object>> readXLSmap(String path) {
        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();
        try {
            Workbook workbook = Workbook.getWorkbook(new File(path));
            Sheet sheet = workbook.getSheet(0);
            sheet.getColumns();
            int rows = sheet.getRows();
            for (int i = 1; i < rows; i++) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("tagUii", sheet.getCell(0, i).getContents());
                arrayList.add(hashMap);
            }
            workbook.close();
        } catch (Exception unused) {
        }
        return arrayList;
    }

    public static List<ArrayList<String>> readXLSX(String path) {
        String nextText;
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        try {
            ZipFile zipFile = new ZipFile(new File(path));
            InputStream inputStream = zipFile.getInputStream(zipFile.getEntry("xl/sharedStrings.xml"));
            XmlPullParser newPullParser = Xml.newPullParser();
            newPullParser.setInput(inputStream, "utf-8");
            for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
                if (eventType == 2 && newPullParser.getName().equalsIgnoreCase("t")) {
                    arrayList.add(newPullParser.nextText());
                }
            }
            InputStream inputStream2 = zipFile.getInputStream(zipFile.getEntry("xl/worksheets/sheet1.xml"));
            XmlPullParser newPullParser2 = Xml.newPullParser();
            newPullParser2.setInput(inputStream2, "utf-8");
            ArrayList arrayList3 = null;
            boolean z = false;
            for (int eventType2 = newPullParser2.getEventType(); eventType2 != 1; eventType2 = newPullParser2.next()) {
                if (eventType2 == 2) {
                    String name = newPullParser2.getName();
                    if (name.equalsIgnoreCase("row")) {
                        arrayList3 = new ArrayList();
                        arrayList2.add(arrayList3);
                    } else if (name.equalsIgnoreCase("c")) {
                        z = newPullParser2.getAttributeValue(null, "t") != null;
                    } else if (name.equalsIgnoreCase("v") && (nextText = newPullParser2.nextText()) != null) {
                        if (z) {
                            arrayList3.add((String) arrayList.get(Integer.parseInt(nextText)));
                        } else {
                            arrayList3.add(nextText);
                        }
                    }
                } else if (eventType2 == 3) {
                    newPullParser2.getName().equalsIgnoreCase("row");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList2;
    }

    public static boolean writeXLS(String path, List<Object> table) {
        File createXLS = createXLS(path);
        if (createXLS == null) {
            return false;
        }
        try {
            return addData(createXLS, table);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (RowsExceededException e2) {
            e2.printStackTrace();
            return false;
        } catch (WriteException e3) {
            e3.printStackTrace();
            return false;
        }
    }

    private static File createXLS(String path) {
        File file = null;
        try {
            try {
                File file2 = new File(path);
                try {
                    if (file2.exists()) {
                        return file2;
                    }
                    WritableWorkbook createWorkbook = Workbook.createWorkbook(file2);
                    createWorkbook.createSheet("sheet1", 0);
                    createWorkbook.write();
                    createWorkbook.close();
                    return file2;
                } catch (Exception e) {
                    e = e;
                    file = file2;
                    e.printStackTrace();
                    return file;
                } catch (Throwable unused) {
                    file = file2;
                    return file;
                }
            } catch (Exception e2) {
                e = e2;
            }
        } catch (Throwable unused2) {
            return file;
        }
    }

    private static boolean addData(File file, List<Object> table) throws IOException, RowsExceededException, WriteException {
        Workbook workbook;
        try {
            workbook = Workbook.getWorkbook(file);
        } catch (BiffException e) {
            e.printStackTrace();
            workbook = null;
        }
        WritableWorkbook createWorkbook = Workbook.createWorkbook(file, workbook);
        WritableSheet sheet = createWorkbook.getSheet(0);
        int rows = sheet.getRows();
        for (int i = 0; i < table.size(); i++) {
            List list = (List) table.get(i);
            for (int i2 = 0; i2 < list.size(); i2++) {
                sheet.addCell(new Label(i2, rows + i, (String) list.get(i2)));
            }
        }
        createWorkbook.write();
        createWorkbook.close();
        workbook.close();
        return true;
    }
}
