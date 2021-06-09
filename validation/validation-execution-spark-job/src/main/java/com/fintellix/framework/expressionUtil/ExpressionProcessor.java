package com.fintellix.framework.expressionUtil;

import java.io.Serializable;
import java.util.List;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.fintellix.framework.SpEL.ExpressionEvaluatorContext;
import com.fintellix.framework.dto.ExpressionStatus;

/**
 * @author Sanketh Noolee
 *
 */
public class ExpressionProcessor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ExpressionStatus expressionStatus;
	public static final SpelExpressionParser parser = new SpelExpressionParser();
	public ExpressionProcessor(ExpressionStatus es) {
		this.expressionStatus=es;
	}
	private List<ExpressionStatus> esList;
	public ExpressionProcessor(List<ExpressionStatus> em) {
		this.esList=em;
	}

	public MapFunction<Row, Row> mf = new MapFunction<Row,Row>(){

		
		private static final long serialVersionUID = 1L;
		@Override
		public Row call(Row value) throws Exception {
			int valId = value.fieldIndex("validationId");
			int exIndex = value.fieldIndex("expression");
			int res = value.fieldIndex("validationResult");
			String replacedExpression = expressionStatus.getSpelExpression();
			int columnIndex;
			for(String c : expressionStatus.getUsedColumnsDataType().keySet()) {
				columnIndex = value.fieldIndex(c);
				replacedExpression = replacedExpression
						.replace(expressionStatus.getReplacedColumnMapping().get(c), value.get(columnIndex)==null?"null":value.get(columnIndex).toString());
			
			}
			
			
			Integer len = value.length();
			Object[] newRow = new Object[len];
			for(int i=0;i<len;i++) {
				if(i==exIndex) {
					newRow[i]=replacedExpression;
				}else if(i==valId) {
					newRow[i]=expressionStatus.getExprId();
				}else if(i==res) {
					try {
						newRow[i]= parser.parseExpression(replacedExpression).getValue(ExpressionEvaluatorContext.context, String.class);
		        	}catch(Throwable e) {
		        		//e.printStackTrace();
		        		System.out.println("Expression evaluation failed for --> "+replacedExpression);
		        		newRow[i]= "false";
		        	}
				}
				else {
					newRow[i]=value.get(i);
				}
			}
			Row r1 = RowFactory.create(newRow);
			return r1;
		}

	};
	
	public MapFunction<Row, Row> multiColumns = new MapFunction<Row,Row>(){

		
		private static final long serialVersionUID = 1L;
		@Override
		public Row call(Row value) throws Exception {
			Integer len = value.length();
			Object[] newRow=new Object[len];
			for(int i=0;i<len;i++) {
				newRow[i]=value.get(i);
			}
			Boolean rowEval = Boolean.TRUE;
			for(ExpressionStatus expressionStatus :esList) {
				int exIndex = value.fieldIndex("expression_"+expressionStatus.getExprId());
				int res = value.fieldIndex("validationResult_"+expressionStatus.getExprId());
				String replacedExpression = expressionStatus.getSpelExpression();
				int columnIndex;
				for(String c : expressionStatus.getUsedColumnsDataType().keySet()) {
					columnIndex = value.fieldIndex(c);
					replacedExpression = replacedExpression
							.replace(expressionStatus.getReplacedColumnMapping().get(c), value.get(columnIndex)==null?"null":value.get(columnIndex).toString());

				}
				newRow[exIndex]=replacedExpression;
				try {
					newRow[res]= parser.parseExpression(replacedExpression).getValue(ExpressionEvaluatorContext.context, String.class);
					if(rowEval && !(Boolean.parseBoolean(newRow[res]+"")) ) {
						newRow[value.fieldIndex("RowResult")]="FALSE";
					}
	        	}catch(Throwable e) {
	        		//e.printStackTrace();
	        		System.out.println("Expression evaluation failed for --> "+replacedExpression);
	        		newRow[res]= "false";
	        		if(rowEval) {
						newRow[value.fieldIndex("RowResult")]="FALSE";
					}
	        	}
			}

			return RowFactory.create(newRow);
		}

	};

}
