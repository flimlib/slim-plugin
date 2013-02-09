/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.analysis.batch.ui;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.awt.GridLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import loci.slim.analysis.HistogramStatistics;

/**
 *
 * @author aivar
 */
public class StatisticsPanel extends JPanel {
	private static final String NAN = "NaN";
    private static final MathContext context = new MathContext(4, RoundingMode.FLOOR);
	private static final Font LABEL_FONT = new Font("Sans Serif", Font.PLAIN, 11);
	
    public StatisticsPanel(HistogramStatistics histogramStatistics) {
		super();
		setLayout(new GridLayout(0, 3));

		// column 0 row 0
		JLabel titleLabel = new Label("" + histogramStatistics.getTitle());
		add(titleLabel);
		
		// column 1 row 0
		JLabel q1Label = new Label("q1: " + showParameter(histogramStatistics.getFirstQuartile()));
		add(q1Label);
		
		// column 2 row 0
		JLabel histoLabel = new Label("histogram");
		add(histoLabel);
		
		// column 0 row 1
		JLabel minLabel = new Label("min: " + showParameter(histogramStatistics.getMin()));
		add(minLabel);
		
		// column 1 row 1
		JLabel medianLabel = new Label("median: " + showParameter(histogramStatistics.getMedian()));
		add(medianLabel);
		
		// column 2 row 1
		JLabel histoBinsLabel = new Label("bins: " + histogramStatistics.getHistogram().length);
		add(histoBinsLabel);

		// column 0 row 2
		JLabel maxLabel = new Label("max: " + showParameter(histogramStatistics.getMax()));
		add(maxLabel);
		
		// column 1 row 2
		JLabel q3Label = new Label("q3: " + showParameter(histogramStatistics.getThirdQuartile()));
		add(q3Label);
		
		// column 2 row 2
		JLabel histoMinLabel = new Label("min: " + showParameter(histogramStatistics.getMinRange()));
		add(histoMinLabel);

		// column 0 row 3
		JLabel countLabel = new Label("count: " + histogramStatistics.getCount());
		add(countLabel);
		
		// column 1 row 3
		JLabel meanLabel = new Label("mean: " + showParameter(histogramStatistics.getMean()));
		add(meanLabel);
		
		// column 2 row 3
		JLabel histoMaxLabel = new Label("max: " + showParameter(histogramStatistics.getMaxRange()));
		add(histoMaxLabel);
		
		// column 0 row 4
		add(new JLabel(""));
		
		// column 1 row 4
		JLabel stdDevLabel = new Label("std dev: " + showParameter(histogramStatistics.getStandardDeviation()));
		add(stdDevLabel);
		
		// column 2 row 4
		JLabel histoCountLabel = new Label("count: " + histogramStatistics.getHistogramCount());
		add(histoCountLabel);
		
		revalidate();
    }

	private String showParameter(double parameter) {
		String returnValue = NAN;
		if (!Double.isNaN(parameter)) {
			returnValue = BigDecimal.valueOf(parameter).round(context).toEngineeringString();
		}
        return returnValue;
	}
	
	private class Label extends JLabel {
		public Label(String text) {
			super(text);
			setFont(LABEL_FONT);
		}
	}
}
