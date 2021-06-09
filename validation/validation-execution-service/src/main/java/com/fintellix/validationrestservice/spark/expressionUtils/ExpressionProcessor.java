/**
 * 
 */
package com.fintellix.validationrestservice.spark.expressionUtils;

import java.io.Serializable;
import java.util.Map;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.fintellix.validationrestservice.core.evaulator.spEL.ExpressionEvaluatorContext;
import com.fintellix.validationrestservice.core.executor.ExpressionStatus;
import com.fintellix.validationrestservice.core.executor.ValidationExecutionGroup;
import com.fintellix.validationrestservice.core.executor.ValidationExecutionGroups;

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

	public MapFunction<Row, Row> mf = new MapFunction<Row,Row>(){

		/**
		 * todo sanketh
		 * done - change based on maps
		 * testing load on modifying rows
		 * use sumeeth util for replace by datatype 
		 */
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
}
