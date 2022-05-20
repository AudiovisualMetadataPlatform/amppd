package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.model.MgmCategory;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.MgmTool;
import edu.indiana.dlib.amppd.repository.MgmCategoryRepository;
import edu.indiana.dlib.amppd.repository.MgmScoringRepository;
import edu.indiana.dlib.amppd.repository.MgmToolRepository;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.web.MgmEvaluationCategory;
import edu.indiana.dlib.amppd.web.MgmEvaluationResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MgmEvaluationServiceImpl implements MgmEvaluationService {

	@Autowired
	MgmCategoryRepository mgmEvaluationCategory;

	@Autowired
	MgmScoringRepository mgmScoringRepository;

	@Autowired
	MgmToolRepository mgmToolRepository;

	@Override
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
	public boolean saveMgmEvaluation(MultipartFile file) {

		try {
			XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
			XSSFSheet categorySheet = workbook.getSheetAt(0);
			inserDataToCategory(categorySheet);
			XSSFSheet toolSheet = workbook.getSheetAt(2);
			inserDataToTool(toolSheet);
			XSSFSheet scoringSheet = workbook.getSheetAt(1);
			inserDataToScoringTool(scoringSheet);
			

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	@Override
	public MgmEvaluationResponse getAllMgmEvaluationDetails() {
		
		List<MgmCategory> categoryList = (List<MgmCategory>) mgmEvaluationCategory.findAll();
		
		return null;
	}

	private void inserDataToCategory(XSSFSheet datatypeSheet) {
		for (int i = 1; i < 8; i++) {
			try {
				XSSFRow row = datatypeSheet.getRow(i);

				MgmCategory category = new MgmCategory();

				category.setSectionId(row.getCell(1) != null ? row.getCell(1).getStringCellValue() : " ");

				category.setSectionName(row.getCell(2) != null ? row.getCell(2).getStringCellValue() : " ");
				category.setDescription(row.getCell(3) != null ? row.getCell(3).getStringCellValue() : " ");

				mgmEvaluationCategory.save(category);
			} catch (Exception e) {
				log.error("Error whie inserting mgmEvaluationCategory excel data into database table" + e);
			}

		}
	}

	private void inserDataToScoringTool(XSSFSheet datatypeSheet) {
		for (int i = 1; i < 15; i++) {
			try {
				XSSFRow row = datatypeSheet.getRow(i);

				MgmScoringTool scoringTool = new MgmScoringTool();

				scoringTool.setName(row.getCell(1) != null ? row.getCell(1).getStringCellValue() : " ");
				scoringTool.setDescription(row.getCell(2) != null ? row.getCell(2).getStringCellValue() : " ");
				scoringTool.setVersion(row.getCell(3) != null ? row.getCell(3).getStringCellValue() : " ");
				scoringTool.setUpgradeDate(row.getCell(4) != null ? row.getCell(4).getDateCellValue() : new Date());
				scoringTool
						.setWorkflowResultDataType(row.getCell(5) != null ? row.getCell(5).getStringCellValue() : " ");
				scoringTool.setGroundTruthFormat(row.getCell(6) != null ? row.getCell(6).getStringCellValue() : " ");
				scoringTool.setParameters(row.getCell(7) != null ? row.getCell(7).getStringCellValue() : "{}");
				scoringTool.setScriptPath(row.getCell(8) != null ? row.getCell(8).getStringCellValue() : " ");
				//scoringTool.setCategory(row.getCell(9) != null ? row.getCell(9).getStringCellValue() : " ");
				scoringTool.setMgmToolId(row.getCell(10) != null ? row.getCell(10).getStringCellValue() : " ");
				mgmScoringRepository.save(scoringTool);
			} catch (Exception e) {
				log.error("Error whie inserting mgmEvaluationScoringTool excel data into database table" + e);
			}
		}

	}

	private void inserDataToTool(XSSFSheet datatypeSheet) {
		for (int i = 1; i < 12; i++) {
			try {
				XSSFRow row = datatypeSheet.getRow(i);

				MgmTool tool = new MgmTool();

				tool.setToolId(row.getCell(0) != null ? row.getCell(0).getStringCellValue() : " ");
				tool.setMgmName(row.getCell(1) != null ? row.getCell(1).getStringCellValue() : " ");
				tool.setUpgradeDate(new Date());
				tool.setVersion("1.0");

				mgmToolRepository.save(tool);

			} catch (Exception e) {
				log.error("Error whie inserting mgmEvaluationTool excel data into database table" + e);
			}
		}
	}

	
}
