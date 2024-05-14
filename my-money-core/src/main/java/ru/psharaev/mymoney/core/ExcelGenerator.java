package ru.psharaev.mymoney.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.OutputStream;

@Slf4j
@Service
public class ExcelGenerator {

    public OutputStream generate(long userId) {
//        try ( Workbook workbook = new XSSFWorkbook()){
//            new ArrayStream
//            workbook.write();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        return null;
    }
}
