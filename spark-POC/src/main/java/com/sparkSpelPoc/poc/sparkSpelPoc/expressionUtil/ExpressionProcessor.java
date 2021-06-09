/**
 * 
 */
package com.sparkSpelPoc.poc.sparkSpelPoc.expressionUtil;

import java.io.Serializable;
import java.util.Map;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

/**
 * @author Sanketh Noolee
 *
 */
public class ExpressionProcessor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Map<Integer, String> valIdsExpressionMap;
	private static Map<Integer, String> valIdsColumnsUsedMap;


	public ExpressionProcessor(Map<Integer, String> _valIdsExpressionMap, Map<Integer, String> _valIdsColumnsUsedMap) {
		this.valIdsExpressionMap = _valIdsExpressionMap;
		this.valIdsColumnsUsedMap = _valIdsColumnsUsedMap;
	}

	public MapFunction<Row, Row> mf = new MapFunction<Row,Row>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Row call(Row value) throws Exception {
			// TODO Auto-generated method stub
			int exIndex = value.fieldIndex("expression");
			int orgId = value.fieldIndex("DIRECTORY_NAME");
			int userId = value.fieldIndex("DIRECTORY_DESC");
			value.length();
			String replacedExpression = value.getString(exIndex);
			if(value.get(orgId) instanceof String) {
				
			}
			replacedExpression = replacedExpression
					.replace("DIRECTORY_NAME", value.get(orgId).toString())
					.replace("DIRECTORY_DESC", value.get(userId)==null?"null":value.get(userId).toString());

			Integer len = value.length();
			Object[] newRow = new Object[len];
			for(int i=0;i<len;i++) {
				if(i==exIndex) {
					newRow[i]=replacedExpression;
				}else {
					newRow[i]=value.get(i);
				}
			}
			Row r1 = RowFactory.create(newRow);
			return r1;
		}

	};
}
