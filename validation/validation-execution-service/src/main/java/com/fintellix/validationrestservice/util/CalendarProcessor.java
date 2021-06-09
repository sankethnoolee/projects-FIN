/**
 * 
 */
package com.fintellix.validationrestservice.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.validationrestservice.core.AbstractPropertyLoader;
import com.fintellix.validationrestservice.util.connectionManager.PersistentStoreManager;

/**
 * @author vishwanath.varanasi
 * 
 */
public class CalendarProcessor extends AbstractPropertyLoader {

	private static final String CONFIG = "calendar.properties";

	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarProcessor.class);
	private static final CalendarProcessor instance = new CalendarProcessor();

	private PeriodIdCalculator periodIdCalculator;

	public enum Type {
		DAY, MONTH, YEAR, QUARTER, HALFYEAR
	}

	static class Span {

		private int beginMonth;
		private int endMonth;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Span [beginMonth=");
			builder.append(beginMonth);
			builder.append(", endMonth=");
			builder.append(endMonth);
			builder.append("]");
			return builder.toString();
		}

		/**
		 * @param beginMonth
		 * @param endMonth
		 */
		public Span(int beginMonth, int endMonth) {
			super();
			this.beginMonth = beginMonth;
			this.endMonth = endMonth;
		}

		/**
		 * @return the beginMonth
		 */
		public int getBeginMonth() {
			return beginMonth;
		}

		/**
		 * @param beginMonth the beginMonth to set
		 */
		public void setBeginMonth(int beginMonth) {
			this.beginMonth = beginMonth;
		}

		/**
		 * @return the endMonth
		 */
		public int getEndMonth() {
			return endMonth;
		}

		/**
		 * @param endMonth the endMonth to set
		 */
		public void setEndMonth(int endMonth) {
			this.endMonth = endMonth;
		}

	}

	static class PeriodIdCalculator {
		private FinancialCalendar financialCalendar;
		private Map<String, CalendarEntry> defaultCalendar = new ConcurrentHashMap<String, CalendarEntry>();

		void parse(JSONObject jsonObject) {

			JSONObject jSonBlock = (JSONObject) jsonObject.get("financial_calendar_start");
			financialCalendar = new FinancialCalendar();
			financialCalendar.setStartDay(((Long) jSonBlock.get("day")).intValue());
			financialCalendar.setStartMonth(((Long) jSonBlock.get("month")).intValue());

			jSonBlock = (JSONObject) jsonObject.get("financial_calendar_end");
			financialCalendar.setEndDay(((Long) jSonBlock.get("day")).intValue());
			financialCalendar.setEndMonth(((Long) jSonBlock.get("month")).intValue());

			jSonBlock = (JSONObject) jsonObject.get("default_calendar");

// process calendar configuration.
			process(jSonBlock.entrySet(), "default_calendar");
			jSonBlock = (JSONObject) jsonObject.get("financial_calendar");
			process(jSonBlock.entrySet(), "financial_calendar");
			jSonBlock = (JSONObject) jsonObject.get("basic_calendar");
			process(jSonBlock.entrySet(), "basic_calendar");
			jSonBlock = (JSONObject) jsonObject.get("special_calendar");
			process(jSonBlock.entrySet(), "special_calendar");

		}

		@SuppressWarnings("rawtypes")
		private void process(Set entrySet, String calType) {
			Entry entry;

			JSONObject jSon = null;
			int startMonth = this.financialCalendar.getStartMonth();
			int endMonth = this.financialCalendar.getEndMonth();
			for (Object o : entrySet) {
				entry = (Entry) o;
				jSon = (JSONObject) entry.getValue();
				CalendarEntry c = null;
				if (calType.equalsIgnoreCase("special_calendar")) {
					Integer multiplyOffSetByMonths = null;
					Integer addOffSetByDays = null;
					String query = null;
					if (jSon.get("multiply_offset_by_months") != null) {
						multiplyOffSetByMonths = ((Long) jSon.get("multiply_offset_by_months")).intValue();
					}
					if (jSon.get("add_offset_by_days") != null) {
						addOffSetByDays = ((Long) jSon.get("add_offset_by_days")).intValue();
					}
					if (jSon.get("query") != null) {
						query = jSon.get("query").toString();
					}

// SpecialCalendar(Long periodId, int minusDays, String
// query, int addDays, int multiplyByMonths,
// int indexReq, int paramCount)

					String span = "";
					if (jSon.get("span") != null) {
						span = jSon.get("span").toString();
					}
					c = new SpecialCalendar(((Long) jSon.get("periodId")),
							((Long) jSon.get("minus_offset_by_days")).intValue(), query, addOffSetByDays,
							multiplyOffSetByMonths, ((Long) jSon.get("index_req")).intValue(),
							((Long) jSon.get("no_of_parameter")).intValue(), span);

				} else {
					if (calType.equalsIgnoreCase("default_calendar")) {
						c = new CalendarEntry();
						c.setIsCalOrFin(Boolean.TRUE);
					} else if (calType.equalsIgnoreCase("financial_calendar")) {
						c = new BusinessCalendarEntry(startMonth, endMonth);
						c.setIsCalOrFin(Boolean.TRUE);
					} else if (calType.equalsIgnoreCase("basic_calendar")) {
						c = new BasicCalendar();
						c.setIsCalOrFin(Boolean.FALSE);
					}

// c = (financialCalendar) ? new
// BusinessCalendarEntry(startMonth,
// endMonth) : new CalendarEntry();
					if (jSon.get("multiply_offset_by_days") != null) {
						c.setTimeUnit(((Long) jSon.get("multiply_offset_by_days")).intValue());
						c.setType(Type.DAY);
					}

					else if (jSon.get("multiply_offset_by_months") != null) {
						c.setTimeUnit(((Long) jSon.get("multiply_offset_by_months")).intValue());
						switch (c.getTimeUnit()) {
						case 3: {// QUARTER
							c.setType(Type.QUARTER);
							break;
						}

						case 6: {// HALF YEAR
							c.setType(Type.HALFYEAR);
							break;
						}

						default: { // any other case.
							c.setType(Type.MONTH);
						}
						}
					}

					else if (jSon.get("multiply_offset_by_year") != null) {
						c.setTimeUnit(((Long) jSon.get("multiply_offset_by_year")).intValue());
						c.setType(Type.YEAR);
					}

					if (jSon.get("year_half") != null) {
						c.setYearHalf(((Long) jSon.get("year_half")).intValue());
					}
					if (c.getTimeUnit() < 0) {
						throw new IllegalArgumentException(
								"Must specify multiply_offset_by_year or multiply_offset_by_month or multiply_offset_by_day for "
										+ entry.getKey() + ", exiting.");
					}

					if (jSon.get("span") != null) {
						String span = (String) jSon.get("span");
						if (span.equalsIgnoreCase("start")) {
							c.setStartOfMonth(Boolean.TRUE);
							c.setEndOfMonth(Boolean.FALSE);

						}

						else if (span.equalsIgnoreCase("end")) {
							c.setStartOfMonth(Boolean.FALSE);
							c.setEndOfMonth(Boolean.TRUE);
						}

						else {
							throw new IllegalArgumentException();
						}

					}

					else {
						c.setStartOfMonth(Boolean.FALSE);
						c.setEndOfMonth(Boolean.FALSE);
					}
					if (jSon.get("day_of_month") != null) {
						c.setDayOfMonth(((Long) jSon.get("day_of_month")).intValue());
					}
					if (jSon.get("day_of_month") != null) {
						c.setDayOfMonth(((Long) jSon.get("day_of_month")).intValue());
					}
					if (jSon.get("day_of_week") != null) {
						c.setDayOfWeek(((Long) jSon.get("day_of_week")).intValue());
					}
					if (jSon.get("add_offset_by_days") != null) {
						c.setAddDays(((Long) jSon.get("add_offset_by_days")).intValue());
					}
					if (jSon.get("minus_offset_by_days") != null) {
						c.setMinusDays(((Long) jSon.get("minus_offset_by_days")).intValue());
					}
				}

				defaultCalendar.put((String) entry.getKey(), c);

			}

		}

		String calculate(Long periodId, String timeDimensionType, int offset, Integer solutionId) throws Throwable {
			CalendarEntry c = defaultCalendar.get(timeDimensionType);
			if (c == null) {
				throw new IllegalArgumentException(timeDimensionType);
			}
			SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
			return s.format(c.apply(s.parse(String.valueOf(periodId)), offset,solutionId));
		}

	}

	static class BasicCalendar extends CalendarEntry {

		protected int timeUnit = -1;
		protected int dayOfMonth = -1;
		protected int dayOfWeek = -1;
		protected int addDays = -1;
		protected int minusDays = -1;
		protected Boolean startOfMonth;
		protected Boolean endOfMonth;
		protected Type type;

		protected Integer startMonth = 1;
		protected Integer endMonth = 12;

		protected Integer[] MONTH_TO_QUARTER;
		protected Integer[] MONTH_TO_HALFYEAR;

		protected Span[] quarterSpans;
		protected Span[] halfYearSpans;

		public BasicCalendar() {
// setup quarterly and yearly roster.
// do nothing

		}

		/**
		 * @param timeUnit the timeUnit to set
		 */
		public void setTimeUnit(int timeUnit) {
			this.timeUnit = timeUnit;
		}

		/**
		 * @param dayOfMonth the dayOfMonth to set
		 */
		public void setDayOfMonth(int dayOfMonth) {
			this.dayOfMonth = dayOfMonth;
		}

		/**
		 * @param dayOfWeek the dayOfWeek to set
		 */
		public void setDayOfWeek(int dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(Type type) {
			this.type = type;
		}

		/**
		 * @return the timeUnit
		 */
		public int getTimeUnit() {
			return timeUnit;
		}

		/**
		 * @return the dayOfMonth
		 */
		public int getDayOfMonth() {
			return dayOfMonth;
		}

		/**
		 * @return the dayOfWeek
		 */
		public int getDayOfWeek() {
			return dayOfWeek;
		}

		/**
		 * @return the type
		 */
		public Type getType() {
			return type;
		}

		/**
		 * @return the startOfMonth
		 */
		public Boolean getStartOfMonth() {
			return startOfMonth;
		}

		/**
		 * @param startOfMonth the startOfMonth to set
		 */
		public void setStartOfMonth(Boolean startOfMonth) {
			this.startOfMonth = startOfMonth;
		}

		/**
		 * @return the endOfMonth
		 */
		public Boolean getEndOfMonth() {
			return endOfMonth;
		}

		/**
		 * @param endOfMonth the endOfMonth to set
		 */
		public void setEndOfMonth(Boolean endOfMonth) {
			this.endOfMonth = endOfMonth;
		}

		public int getAddDays() {
			return addDays;
		}

		public void setAddDays(int addDays) {
			this.addDays = addDays;
		}

		public int getMinusDays() {
			return minusDays;
		}

		public void setMinusDays(int minusDays) {
			this.minusDays = minusDays;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CalendarEntry [timeUnit=");
			builder.append(timeUnit);
			builder.append(", dayOfMonth=");
			builder.append(dayOfMonth);
			builder.append(", dayOfWeek=");
			builder.append(dayOfWeek);
			builder.append(", startOfMonth=");
			builder.append(startOfMonth);
			builder.append(", endOfMonth=");
			builder.append(endOfMonth);
			builder.append(", type=");
			builder.append(type);
			builder.append(", startMonth=");
			builder.append(startMonth);
			builder.append(", endMonth=");
			builder.append(endMonth);
			builder.append("]");
			return builder.toString();
		}

		public Date apply(Date d, int offset,Integer solutionId) {
// basic
			DateTime x = new DateTime(d.getTime());
			DateTime y = x.dayOfMonth().withMaximumValue();
			if (offset > 0) {// handle offset calculations, if offset is
// positive.
				if (type == Type.DAY) {
					x = x.minusDays(timeUnit * offset);

				}

				else if ((type == Type.MONTH) || (type == Type.HALFYEAR) || (type == Type.QUARTER)) {
					if (x.equals(y) && !startOfMonth) {
						endOfMonth = Boolean.TRUE;
					}
					x = x.minusMonths(timeUnit * offset);
				}

				else if ((type == Type.YEAR)) {
					if (x.equals(y) && !startOfMonth) {
						endOfMonth = Boolean.TRUE;
					}
					x = x.minusYears(timeUnit * offset);
				}
			} else {
				return d;
			}

			if (startOfMonth) {
				x = x.minusDays(x.getDayOfMonth() - (x.dayOfMonth().withMinimumValue()).getDayOfMonth());

			} else if (endOfMonth) {

				x = x.minusDays(x.getDayOfMonth() - (x.dayOfMonth().withMaximumValue()).getDayOfMonth());
			}

			if (minusDays > 0) {
				x = x.minusDays(1 * minusDays);
			}
			x = applyDayOfWeek(applyDayOfMonth(x));
			if (addDays > 0) {
				x = x.plusDays(1 * addDays);
			}
			return x.toDate();
		}

		private DateTime applyDayOfWeek(DateTime d) {
			if (dayOfWeek == -1)
				return d;

			if (endOfMonth) {
				if (d.getDayOfWeek() < dayOfWeek) {
					d = d.minusDays(7);
				}

			}
			return d.withDayOfWeek(dayOfWeek);

		}

		private DateTime applyDayOfMonth(DateTime d) {
			if (dayOfMonth == -1)
				return d;// not configured.

			return d.plusDays(dayOfMonth);

		}

	}

	static class BusinessCalendarEntry extends CalendarEntry {
		public BusinessCalendarEntry(int startMonth, int endMonth) {
// setup quarterly and yearly roster.
			this.startMonth = startMonth;
			this.endMonth = endMonth;
			quarterSpans = new Span[4];
			Map<Integer, Integer> monthLinkage = new HashMap<Integer, Integer>();
			int x = startMonth;
			quarterSpans = new Span[4];
			for (int i = 1; i <= 4; i++) {
				int k = x;
				if (x > 12) {
					k = 1;
					x = 1;
				}

				monthLinkage.put(x++, i);
				if (x > 12) {
					x = 1;
				}
				monthLinkage.put(x++, i);
				if (x > 12) {
					x = 1;
				}
				monthLinkage.put(x, i);
				quarterSpans[i - 1] = new Span(k, x);
				x++;
			}
			MONTH_TO_QUARTER = new Integer[12];
			MONTH_TO_HALFYEAR = new Integer[12];
			for (int i = 1; i <= 12; i++) {

				MONTH_TO_QUARTER[i - 1] = monthLinkage.get(i);
				MONTH_TO_HALFYEAR[i - 1] = (MONTH_TO_QUARTER[i - 1] > 2) ? 2 : 1;

			}
// initialize half year spans
			halfYearSpans = new Span[2];
			halfYearSpans[0] = new Span(quarterSpans[0].beginMonth, quarterSpans[1].endMonth);// include
// start
// month
// also !
			halfYearSpans[1] = new Span(quarterSpans[2].beginMonth, quarterSpans[3].endMonth);// include
// //
// roll
// over.

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("BusinessCalendarEntry [timeUnit=");
			builder.append(timeUnit);
			builder.append(", dayOfMonth=");
			builder.append(dayOfMonth);
			builder.append(", dayOfWeek=");
			builder.append(dayOfWeek);
			builder.append(", startOfMonth=");
			builder.append(startOfMonth);
			builder.append(", endOfMonth=");
			builder.append(endOfMonth);
			builder.append(", type=");
			builder.append(type);
			builder.append(", startMonth=");
			builder.append(startMonth);
			builder.append(", endMonth=");
			builder.append(endMonth);
			builder.append("]");
			return builder.toString();
		}

	}

	static class CalendarEntry {

		protected int timeUnit = -1;
		protected Long periodId;
		protected int dayOfMonth = -1;
		protected int dayOfWeek = -1;
		protected int addDays = -1;
		protected int minusDays = -1;
		protected int yearHalf = -1;
		protected Boolean isCalOrFin = Boolean.FALSE;
		protected Boolean startOfMonth;
		protected Boolean endOfMonth;
		protected Type type;

		protected Integer startMonth = 1;
		protected Integer endMonth = 12;

		protected Integer[] MONTH_TO_QUARTER;
		protected Integer[] MONTH_TO_HALFYEAR;

		protected Span[] quarterSpans;
		protected Span[] halfYearSpans;

		public CalendarEntry() {
// setup quarterly and yearly roster.
			MONTH_TO_QUARTER = new Integer[12];
			MONTH_TO_HALFYEAR = new Integer[12];

			Double d = null;
			for (int i = 1; i <= 12; i++) {
				d = Double.valueOf(Math.ceil(i / 3.0));
				MONTH_TO_QUARTER[i - 1] = d.intValue();
				d = Math.ceil(i / 6.0);
				MONTH_TO_HALFYEAR[i - 1] = d.intValue();
			}

// initialize quarter spans.
			quarterSpans = new Span[4];
			quarterSpans[0] = new Span(1, 3);
			quarterSpans[1] = new Span(4, 6);
			quarterSpans[2] = new Span(7, 9);
			quarterSpans[3] = new Span(10, 12);

// initialize half year spans
			halfYearSpans = new Span[2];
			halfYearSpans[0] = new Span(1, 6);
			halfYearSpans[1] = new Span(7, 12);

		}

		/**
		 * @param timeUnit the timeUnit to set
		 */
		public void setTimeUnit(int timeUnit) {
			this.timeUnit = timeUnit;
		}

		/**
		 * @param dayOfMonth the dayOfMonth to set
		 */
		public void setDayOfMonth(int dayOfMonth) {
			this.dayOfMonth = dayOfMonth;
		}

		/**
		 * @param dayOfWeek the dayOfWeek to set
		 */
		public void setDayOfWeek(int dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(Type type) {
			this.type = type;
		}

		/**
		 * @return the timeUnit
		 */
		public int getTimeUnit() {
			return timeUnit;
		}

		/**
		 * @return the dayOfMonth
		 */
		public int getDayOfMonth() {
			return dayOfMonth;
		}

		/**
		 * @return the dayOfWeek
		 */
		public int getDayOfWeek() {
			return dayOfWeek;
		}

		/**
		 * @return the type
		 */
		public Type getType() {
			return type;
		}

		/**
		 * @return the startOfMonth
		 */
		public Boolean getStartOfMonth() {
			return startOfMonth;
		}

		/**
		 * @param startOfMonth the startOfMonth to set
		 */
		public void setStartOfMonth(Boolean startOfMonth) {
			this.startOfMonth = startOfMonth;
		}

		/**
		 * @return the endOfMonth
		 */
		public Boolean getEndOfMonth() {
			return endOfMonth;
		}

		/**
		 * @param endOfMonth the endOfMonth to set
		 */
		public void setEndOfMonth(Boolean endOfMonth) {
			this.endOfMonth = endOfMonth;
		}

		public int getAddDays() {
			return addDays;
		}

		public void setAddDays(int addDays) {
			this.addDays = addDays;
		}

		public int getMinusDays() {
			return minusDays;
		}

		public void setMinusDays(int minusDays) {
			this.minusDays = minusDays;
		}

		public int getYearHalf() {
			return yearHalf;
		}

		public void setYearHalf(int yearHalf) {
			this.yearHalf = yearHalf;
		}

		public Long getPeriodId() {
			return periodId;
		}

		/**
		 * @return the isCalOrFin
		 */
		public Boolean getIsCalOrFin() {
			return isCalOrFin;
		}

		/**
		 * @param isCalOrFin the isCalOrFin to set
		 */
		public void setIsCalOrFin(Boolean isCalOrFin) {
			this.isCalOrFin = isCalOrFin;
		}

		public void setPeriodId(Long periodId) {
			this.periodId = periodId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CalendarEntry [timeUnit=");
			builder.append(timeUnit);
			builder.append(", periodId=");
			builder.append(periodId);
			builder.append(", dayOfMonth=");
			builder.append(dayOfMonth);
			builder.append(", dayOfWeek=");
			builder.append(dayOfWeek);
			builder.append(", addDays=");
			builder.append(addDays);
			builder.append(", minusDays=");
			builder.append(minusDays);
			builder.append(", yearHalf=");
			builder.append(yearHalf);
			builder.append(", isCalOrFin=");
			builder.append(isCalOrFin);
			builder.append(", startOfMonth=");
			builder.append(startOfMonth);
			builder.append(", endOfMonth=");
			builder.append(endOfMonth);
			builder.append(", type=");
			builder.append(type);
			builder.append(", startMonth=");
			builder.append(startMonth);
			builder.append(", endMonth=");
			builder.append(endMonth);
			builder.append(", MONTH_TO_QUARTER=");
			builder.append(Arrays.toString(MONTH_TO_QUARTER));
			builder.append(", MONTH_TO_HALFYEAR=");
			builder.append(Arrays.toString(MONTH_TO_HALFYEAR));
			builder.append(", quarterSpans=");
			builder.append(Arrays.toString(quarterSpans));
			builder.append(", halfYearSpans=");
			builder.append(Arrays.toString(halfYearSpans));
			builder.append("]");
			return builder.toString();
		}

		private Boolean validCase(DateTime x, Date d) {
			SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
			Long newdate = Long.parseLong(s.format(x.toDate()));
			Long oldDate = Long.parseLong(s.format(d));
			return (newdate.longValue() <= oldDate.longValue());
		}

		private DateTime process(DateTime x, int offset) {
			Integer month = (type == Type.MONTH) ? x.getMonthOfYear() : -1;
			if (yearHalf > 0) {
				if (startOfMonth) { // span ?
					if (type == Type.HALFYEAR) {
						Span s = halfYearSpans[yearHalf - 1];
						month = s.getBeginMonth();
					}
				}

				else if (endOfMonth) {
					if (type == Type.HALFYEAR) {
						Span s = halfYearSpans[yearHalf - 1];
						month = s.getEndMonth();
					}
				}
			} else {
				if (startOfMonth) { // span ?
					if (type == Type.YEAR) {
						month = startMonth;
					}

					else if (type == Type.HALFYEAR) {
						Span s = halfYearSpans[MONTH_TO_HALFYEAR[x.getMonthOfYear() - 1] - 1];
						month = s.getBeginMonth();
					}

					else if (type == Type.QUARTER) {
						Span s = quarterSpans[MONTH_TO_QUARTER[x.getMonthOfYear() - 1] - 1];
						month = s.getBeginMonth();
					}

				}

				else if (endOfMonth) {

					if (type == Type.YEAR) {
						month = endMonth;
					}

					else if (type == Type.HALFYEAR) {
						Span s = halfYearSpans[MONTH_TO_HALFYEAR[x.getMonthOfYear() - 1] - 1];
						month = s.getEndMonth();
					}

					else if (type == Type.QUARTER) {
						Span s = quarterSpans[MONTH_TO_QUARTER[x.getMonthOfYear() - 1] - 1];
						month = s.getEndMonth();
					}

				}
			}

			if (month > 0) {
				x = x.minusMonths(x.getMonthOfYear() - month);
				if (startOfMonth) {
					x = x.minusDays(x.getDayOfMonth() - (x.dayOfMonth().withMinimumValue()).getDayOfMonth());

				}

				else if (endOfMonth) {

					x = x.minusDays(x.getDayOfMonth() - (x.dayOfMonth().withMaximumValue()).getDayOfMonth());
				}

			}
//if(endOfMonth){
//System.out.println("dummy");
//}
//else
//TODO
			if (minusDays > 0) {
				x = x.minusDays(1 * minusDays);
			}
			x = applyDayOfWeek(applyDayOfMonth(x));
			if (addDays > 0) {
				x = x.plusDays(1 * addDays);
			}
			return x;

		}

		public Date apply(Date d, int offset,Integer solutionId) {
// calendar entry
			this.toString();
			DateTime x = new DateTime(d.getTime());
			DateTime z = x.dayOfMonth().withMaximumValue();
			if (offset > 0) {// handle offset calculations, if offset is
// positive.
				if (type == Type.DAY) {
					if (isCalOrFin) {
						DateTime y = new DateTime(d.getTime());
						y = process(y, 0);
						if (validCase(y, d)) {
							offset = offset - 1;
							if (offset == 0) {
								return y.toDate();
							}
						}
					}
					x = x.minusDays(timeUnit * offset);
					x = process(x, offset);
				}

				else if (type == Type.MONTH) {
					if (isCalOrFin) {
						if (x.equals(z) && !startOfMonth) {
							endOfMonth = Boolean.TRUE;
						}
						DateTime y = new DateTime(d.getTime());
						y = process(y, 0);
						if (validCase(y, d)) {
							offset = offset - 1;
							if (offset == 0) {
								return y.toDate();
							}
						}
					}
					x = x.minusMonths(timeUnit * offset);
					x = process(x, offset);
				} else if ((type == Type.HALFYEAR) || (type == Type.QUARTER)) {
					if (isCalOrFin) {
						if (x.equals(z) && !startOfMonth) {
							endOfMonth = Boolean.TRUE;
						}
						DateTime y = new DateTime(d.getTime());
						y = process(y, 0);
						if (validCase(y, d)) {
							offset = offset - 1;
							if (offset == 0) {
								return y.toDate();
							}
						}
					}
					x = x.minusMonths(timeUnit * offset);
					x = process(x, offset);
				}

				else if ((type == Type.YEAR)) {
					if (isCalOrFin && startOfMonth) {
						DateTime y = new DateTime(d.getTime());
						y = process(y, 0);
						if (validCase(y, d)) {
							offset = offset - 1;
							if (offset == 0) {
								return y.toDate();
							}
						}
					}
					x = x.minusYears(timeUnit * offset);
					x = process(x, offset);
				}
			} else {
				return d;
			}

			return x.toDate();
		}

		private DateTime applyDayOfWeek(DateTime d) {
			if (dayOfWeek == -1)
				return d;

			if (endOfMonth) {
				if (d.getDayOfWeek() < dayOfWeek) {
					d = d.minusDays(7);
				}

			}
			return d.withDayOfWeek(dayOfWeek);

		}

		private DateTime applyDayOfMonth(DateTime d) {
			if (dayOfMonth == -1)
				return d;// not configured.

			return d.plusDays(dayOfMonth);

		}

	}

	static class SpecialCalendar extends CalendarEntry {

		protected Long periodId;
		protected int minusDays;
		protected String query;
		protected int multiplyByMonths;
		protected int addDays;
		protected int paramCount;
		protected int indexReq;
		protected String span;

		public Long getPeriodId() {
			return periodId;
		}

		public void setPeriodId(Long periodId) {
			this.periodId = periodId;
		}

		public int getMinusDays() {
			return minusDays;
		}

		public void setMinusDays(int minusDays) {
			this.minusDays = minusDays;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public int getMultiplyByMonths() {
			return multiplyByMonths;
		}

		public void setMultiplyByMonths(int multiplyByMonths) {
			this.multiplyByMonths = multiplyByMonths;
		}

		public int getAddDays() {
			return addDays;
		}

		public void setAddDays(int addDays) {
			this.addDays = addDays;
		}

		public int getParamCount() {
			return paramCount;
		}

		public void setParamCount(int paramCount) {
			this.paramCount = paramCount;
		}

		public int getIndexReq() {
			return indexReq;
		}

		public void setIndexReq(int indexReq) {
			this.indexReq = indexReq;
		}

		public String getSpan() {
			return span;
		}

		public void setSpan(String span) {
			this.span = span;
		}

		public SpecialCalendar(Long periodId, int minusDays, String query, int addDays, int multiplyByMonths,
				int indexReq, int paramCount, String span) {
			this.periodId = periodId;
			this.minusDays = minusDays;
			this.query = query;
			this.addDays = addDays;
			this.multiplyByMonths = multiplyByMonths;
			this.indexReq = indexReq;
			this.paramCount = paramCount;
			this.span = span;
		}

		public Date apply(Date d, int offset,Integer solutionId) {
// special
			SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
			Date x = d;
			try {
				if (periodId > 0) {
					x = s.parse(String.valueOf(periodId));
				} else if (!(query.equalsIgnoreCase(""))) {
					Connection conn = null;
					PreparedStatement ps = null;
					ResultSet rs = null;

					if (offset > 0 && multiplyByMonths > 0) {// handle offset
// calculations,
// if offset is
// positive.
						DateTime z = new DateTime(x.getTime());
						z = z.minusMonths(multiplyByMonths * offset);
						x = z.toDate();
					}

					if (span.equalsIgnoreCase("start")) {
						DateTime z = new DateTime(x.getTime());
						z = z.minusDays(z.getDayOfMonth() - (z.dayOfMonth().withMinimumValue()).getDayOfMonth());
						x = z.toDate();
					} else if (span.equalsIgnoreCase("end")) {
						DateTime z = new DateTime(x.getTime());
						z = z.minusDays(z.getDayOfMonth() - (z.dayOfMonth().withMaximumValue()).getDayOfMonth());
						x = z.toDate();

					}
					try {
						conn = PersistentStoreManager.getSolutionDBConnection(solutionId);
						ps = conn.prepareStatement(query);
						Integer index = 0;
						for (index = 0; index < paramCount; index++) {
							ps.setInt(index + 1, Integer.parseInt(s.format(x)));
						}
						rs = ps.executeQuery();
						if (rs != null) {
							List<Integer> periodIds = new ArrayList<Integer>();
							while (rs.next()) {
								periodIds.add(rs.getInt(1));
							}

							if (periodIds.size() > 0) {
								if (indexReq > 0) {
									if (offset < periodIds.size()) {
										x = s.parse(String.valueOf(periodIds.get(offset)));
									}
								} else {
									x = s.parse(String.valueOf(periodIds.get(0)));
								}
								if (addDays > 0) {

									DateTime z = new DateTime(x.getTime());
									x = z.plusDays(addDays).toDate();
								}
							} else {
								x = d;
							}

						} else {
							x = d;
						}
					} catch (Throwable e) {

						LOGGER.error("Error occured " + e);

					} finally {
						if (rs != null) {

							rs.close();

						}
						if (ps != null) {
							ps.close();
						}
						if (conn != null) {
							conn.close();
						}

					}
					return x;
				} else {
					x = computeMidMonth(d, minusDays);
				}
				return x;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return x;
		}

		@SuppressWarnings("deprecation")
		private Date computeMidMonth(Date d, int minusDays) {
			if (d.getDate() <= 15) {
				d.setDate(1);
			} else {
				d.setDate(16);
			}
			if (minusDays > 0) {
				DateTime x = new DateTime(d.getTime());
				d = (x.minusDays(minusDays)).toDate();
			}
			return d;

		}

	}

	static class FinancialCalendar {

		private int startDay;
		private int startMonth;

		private int endDay;
		private int endMonth;

		private Map<String, CalendarEntry> calendar = new ConcurrentHashMap<String, CalendarEntry>();

		public FinancialCalendar() {
// do nothing.
		}

		public void add(CalendarEntry entry, String key) {
			calendar.put(key, entry);
		}

		public CalendarEntry get(String key) {
			return calendar.get(key);
		}

		/**
		 * @return the startDay
		 */
		public int getStartDay() {
			return startDay;
		}

		/**
		 * @param startDay the startDay to set
		 */
		public void setStartDay(int startDay) {
			this.startDay = startDay;
		}

		/**
		 * @return the startMonth
		 */
		public int getStartMonth() {
			return startMonth;
		}

		/**
		 * @param startMonth the startMonth to set
		 */
		public void setStartMonth(int startMonth) {
			this.startMonth = startMonth;
		}

		/**
		 * @return the endDay
		 */
		public int getEndDay() {
			return endDay;
		}

		/**
		 * @param endDay the endDay to set
		 */
		public void setEndDay(int endDay) {
			this.endDay = endDay;
		}

		/**
		 * @return the endMonth
		 */
		public int getEndMonth() {
			return endMonth;
		}

		/**
		 * @param endMonth the endMonth to set
		 */
		public void setEndMonth(int endMonth) {
			this.endMonth = endMonth;
		}

	}

	private CalendarProcessor() {
		try {
			this.configFile = CONFIG;
			load(init());
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * @return the instance
	 */
	public static final CalendarProcessor getInstance() {
		return instance;
	}

	@Override
	public void load(JSONObject jsonObject) throws Throwable {
		periodIdCalculator = new PeriodIdCalculator();
		periodIdCalculator.parse(jsonObject);
	}

	/**
	 * Compute period for a pre-configured time dimension type.
	 * 
	 * @param periodId
	 * 
	 * @param periodId
	 * @param minusDays
	 * @param timeDimensionType
	 * @return
	 * @throws Throwable
	 * @throws Throwable
	 */

	public Long getPeriodId(Long periodId, String timeDimensionType, int offset,Integer solutionId) throws Throwable {

		try {
			return Long.parseLong(getPeriodIdAsString(periodId, timeDimensionType, offset,solutionId));
		} catch (IllegalArgumentException e) {
			if (timeDimensionType.equalsIgnoreCase("FORM A REVALUATION RATE")) {
				return null;
			} else if (timeDimensionType.equalsIgnoreCase("QUARTER - LAST REPORTING FRIDAY (NEXT DAY)")) {
				return null;
			} else if (timeDimensionType.equalsIgnoreCase("LAST REPORTING FRIDAY - WEEK/FORTNIGHT/MONTH")) {
				return null;
			} else {
				throw e;
			}
		}
	}

	/**
	 * Compute period for a pre-configured time dimension type.
	 * 
	 * @param periodId
	 * @param timeDimensionType
	 * @param solutionId 
	 * @return
	 * @throws Throwable
	 */
	public String getPeriodIdAsString(Long periodId, String timeDimensionType, int offset, Integer solutionId) throws Throwable {
		return periodIdCalculator.calculate(periodId, StringUtils.strip(timeDimensionType).toUpperCase(), offset,solutionId);

	}

}
