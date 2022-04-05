/*
 * Copyright (c) NanoDefine project. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */

package eu.nanodefine.etool.model.services.view;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.data.Row;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.graphics.Label;
import de.erichseifert.gral.graphics.Location;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.lines.SmoothLineRenderer2D;
import eu.nanodefine.etool.analysis.processors.CMFProcessor;
import eu.nanodefine.etool.analysis.processors.IAnalysisProcessor;
import eu.nanodefine.etool.analysis.processors.SPCTv2Processor;
import eu.nanodefine.etool.model.helpers.managers.ServiceManager;
import eu.nanodefine.etool.model.interfaces.IService;
import eu.nanodefine.etool.model.services.material.MaterialService;
import eu.nanodefine.etool.utilities.classes.Tuple;

/**
 * Service for the creation of plots.
 *
 * TODO extract locales
 * TODO rename density to histogram
 */
@Service
public class PlotService implements IService {

	private final NumberService numberService;

	private final ServiceManager serviceManager;

	private Logger log = LoggerFactory.getLogger(PlotService.class);

	@Autowired
	public PlotService(NumberService numberService,
			ServiceManager serviceManager) {
		this.numberService = numberService;
		this.serviceManager = serviceManager;
	}

	/**
	 * Adds markers for the expected size range to the given plot.
	 */
	private void addExpectedSizeRange(XYPlot plot, IAnalysisProcessor processor, Double lowerX,
			Double upperX, Double lowerY, Double upperY) {
		upperY = upperY + (upperY - lowerY) / 10;
		Double[] expectedSizeRange = this.serviceManager.getBean(MaterialService.class)
				.determineExpectedSizeRange(processor.getMethod().getDossier().getMaterials());

		LineRenderer lineDashed = new DefaultLineRenderer2D();
		Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
				new float[] { 9 }, 0);
		lineDashed.setStroke(dashed);

		DataSource dataExpectedSizeRangeLower = null, dataExpectedSizeRangeUpper = null;

		if (expectedSizeRange[0] != null && expectedSizeRange[0] >= lowerX
				&& expectedSizeRange[0] <= upperX) {
			DataTable dataTable = new DataTable(Double.class, Double.class);
			dataTable.add(expectedSizeRange[0], lowerY);
			dataTable.add(expectedSizeRange[0], upperY);
			dataExpectedSizeRangeLower = new DataSeries("Expected particle size range", dataTable);
			plot.add(dataExpectedSizeRangeLower);
			// remove current renderer
			plot.setLineRenderers(dataExpectedSizeRangeLower, new ArrayList<>());
			plot.setLineRenderers(dataExpectedSizeRangeLower, lineDashed);
			// Don't render points
			plot.setPointRenderers(dataExpectedSizeRangeLower, new ArrayList<>());
		}

		if (expectedSizeRange[1] != null && expectedSizeRange[1] <= upperX) {
			DataTable dataTable = new DataTable(Double.class, Double.class);
			dataTable.add(expectedSizeRange[1], lowerY);
			dataTable.add(expectedSizeRange[1], upperY);
			dataExpectedSizeRangeUpper = new DataSeries("Expected particle size range", dataTable);

			plot.add(dataExpectedSizeRangeUpper);
			// remove current renderer
			plot.setLineRenderers(dataExpectedSizeRangeUpper, new ArrayList<>());
			plot.setLineRenderers(dataExpectedSizeRangeUpper, lineDashed);
			// Don't render points
			plot.setPointRenderers(dataExpectedSizeRangeUpper, new ArrayList<>());
		}

		plot.getLegend().clear();
		//plot.setLegendVisible(false);
		plot.setLegendLocation(Location.SOUTH);
		plot.setLegendDistance(6.);
		plot.setInsets(new Insets2D.Double(20, 80, 160, 20));

		if (dataExpectedSizeRangeLower != null || dataExpectedSizeRangeUpper != null) {
			// Legend
			plot.getLegend().add(dataExpectedSizeRangeLower != null ?
					dataExpectedSizeRangeLower : dataExpectedSizeRangeUpper);
			plot.setLegendVisible(true);
		}
	}

	/**
	 * Creates a bar plot for the given labels and data sources.
	 */
	private BarPlot createBarPlot(String xLabel, String yLabel, DataSource... dataSources)
			throws IOException {
		BarPlot plot = new BarPlot(dataSources);

		plot.setInsets(new Insets2D.Double(20, 80, 140, 20));
		plot.getAxisRenderer(XYPlot.AXIS_Y).setLabelDistance(2);
		plot.getAxisRenderer(XYPlot.AXIS_X).setLabel(new Label(xLabel));
		plot.getAxisRenderer(XYPlot.AXIS_Y).setLabel(new Label(yLabel));
		plot.getAxis(XYPlot.AXIS_Y)
				.setMax(plot.getAxis(XYPlot.AXIS_Y).getMax().doubleValue() * 1.1);

		return plot;
	}

	/**
	 * Creates CMF density plot.
	 */
	public byte[] createCMFDensityPlot(CMFProcessor processor) throws IOException {

		DataTable data = new DataTable(Double.class, Double.class);

		double maxY = 0;
		for (Tuple<Double, Double> t : processor.getDensity()) {
			data.add(t.getLeft(), t.getRight());
			maxY = Math.max(maxY, t.getRight());
		}

		Integer d50 = Integer.valueOf(this.numberService.formatNumber(processor.getResult(), 0));

		BarPlot plot = this.createBarPlot("Particle size (nm)", "f(x)", data);
		plot.getAxisRenderer(XYPlot.AXIS_X)
				.setCustomTicks(ImmutableMap.of(processor.getResult(), d50.toString()));

		int lastIndex = processor.getDensity().size() - 1;

		Tuple<Double, Double> first = processor.getDensity().get(0),
				last = processor.getDensity().get(lastIndex);

		plot.setBarWidth(processor.getBinWidth());

		this.addExpectedSizeRange(plot, processor, first.getLeft(), last.getLeft(), 0., maxY);
		plot.getLegend().clear();
		plot.setLegendVisible(false);

		return this.createPlotBytes(plot);
	}

	/**
	 * Creates CMF distribution plot.
	 */
	public byte[] createCMFDistributionPlot(CMFProcessor processor) throws IOException {

		DataTable data = new DataTable(Double.class, Double.class);

		for (Tuple<Double, Double> t : processor.getEntries()) {
			data.add(t.getLeft(), t.getRight());
		}

		DataTable dataMedian = new DataTable(Double.class, Double.class);
		dataMedian.add(processor.getEntries().get(0).getLeft(), .5);
		dataMedian.add(processor.getEntries().get(processor.getEntries().size() - 1).getLeft(), .5);
		DataSeries dataMedianSeries = new DataSeries("Median (D_50)", dataMedian);

		Integer d50 = Integer.valueOf(this.numberService.formatNumber(processor.getResult(), 0));

		XYPlot plot = this.createXYPlot("Particle size (nm)", "F(x)", data, dataMedianSeries);

		int lastIndex = processor.getEntries().size() - 1;

		Tuple<Double, Double> first = processor.getEntries().get(0),
				last = processor.getEntries().get(lastIndex);

		// Don't render points
		plot.setPointRenderers(dataMedianSeries, new ArrayList<>());
		// Mark D50 on X axis
		plot.getAxisRenderer(XYPlot.AXIS_X)
				.setCustomTicks(ImmutableMap.of(processor.getResult(), d50.toString()));

		this.addExpectedSizeRange(plot, processor, first.getLeft(),
				last.getLeft(), first.getRight(), last.getRight());
		plot.getLegend().add(dataMedianSeries);
		plot.setLegendVisible(true);

		return this.createPlotBytes(plot);
	}

	/**
	 * Converts the given {@link XYPlot} into a byte array.
	 */
	private byte[] createPlotBytes(XYPlot plot) throws IOException {
		BufferedImage bImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) bImage.getGraphics();
		DrawingContext context = new DrawingContext(g2d);
		plot.draw(context);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DrawableWriter wr = DrawableWriterFactory.getInstance().get("image/png");
		wr.write(plot, baos, 800, 600);
		baos.flush();
		byte[] bytes = baos.toByteArray();
		baos.close();

		return bytes;
	}

	/**
	 * Creates SP-ICP-MS density plot.
	 *
	 * TODO extract duplicate code
	 */
	public byte[] createSPICPMSDensityPlot(SPCTv2Processor processor) throws IOException {
		TreeMultiset<Double> values = processor.getValues();
		double numValues = values.size();
		DataTable data = new DataTable(Double.class, Double.class);

		double value, maxY = 0;
		for (Multiset.Entry<Double> entry : values.entrySet()) {
			value = entry.getCount() / numValues;
			data.add(entry.getElement(), value);
			maxY = Math.max(maxY, value);
		}

		Integer d50 = Integer.valueOf(this.numberService.formatNumber(processor.getResult(), 0));

		BarPlot plot = this.createBarPlot("Particle size (nm)", "f(x)", data);
		plot.getAxisRenderer(XYPlot.AXIS_X)
				.setCustomTicks(ImmutableMap.of(processor.getResult(), d50.toString()));

		int lastIndex = data.getRowCount() - 1;

		Row first = data.getRow(0), last = data.getRow(lastIndex);

		plot.setBarWidth(this.determineBarWidth((Double) first.get(0), (Double) last.get(0)));

		this.addExpectedSizeRange(plot, processor, (Double) first.get(0),
				(Double) last.get(0), 0., maxY);
		plot.getLegend().clear();
		plot.setLegendVisible(false);

		return this.createPlotBytes(plot);
	}

	/**
	 * Creates SP-ICP-MS distribution plot.
	 */
	public byte[] createSPICPMSDistributionPlot(SPCTv2Processor processor) throws IOException {
		DataTable data = new DataTable(Double.class, Double.class);
		TreeMultiset<Double> values = processor.getValues();
		double numValues = values.size();
		double value = 0;
		for (double i = values.firstEntry().getElement(); i < values.lastEntry().getElement(); i++) {
			value = values.headMultiset(i, BoundType.CLOSED).size() / numValues;
			data.add(i, value);
		}

		DataTable dataMedian = new DataTable(Double.class, Double.class);
		dataMedian.add(data.get(0, 0), .5);
		dataMedian.add(data.get(0, data.getRowCount() - 1), .5);
		DataSeries dataMedianSeries = new DataSeries("Median (D_50)", dataMedian);

		Integer d50 = Integer.valueOf(this.numberService.formatNumber(processor.getResult(), 0));

		XYPlot plot = this.createXYPlot("Particle size (nm)", "F(x)", data, dataMedianSeries);

		int lastIndex = data.getRowCount() - 1;

		Row first = data.getRow(0), last = data.getRow(lastIndex);

		plot.setPointRenderers(dataMedianSeries, new ArrayList<>()); // Don't render points
		plot.getAxisRenderer(XYPlot.AXIS_X)
				.setCustomTicks(ImmutableMap.of(processor.getResult(), d50.toString()));

		this.addExpectedSizeRange(plot, processor, (Double) first.get(0),
				(Double) last.get(0), (Double) first.get(1), (Double) last.get(1));
		plot.getLegend().add(dataMedianSeries);
		plot.setLegendVisible(true);

		return this.createPlotBytes(plot);
	}

	/**
	 * Creates a plot for the given labels and data sources.
	 */
	private XYPlot createXYPlot(String xLabel, String yLabel, DataSource... dataSources) {
		XYPlot plot = new XYPlot(dataSources);
		LineRenderer lines = new SmoothLineRenderer2D();
		((SmoothLineRenderer2D) lines).setSmoothness(1);

		Arrays.stream(dataSources).forEach(t -> plot.setLineRenderers(t, lines));

		plot.setInsets(new Insets2D.Double(20, 80, 140, 20));
		plot.getAxisRenderer(XYPlot.AXIS_Y).setLabelDistance(2);
		plot.getAxisRenderer(XYPlot.AXIS_X).setLabel(new Label(xLabel));
		plot.getAxisRenderer(XYPlot.AXIS_Y).setLabel(new Label(yLabel));
		plot.getAxis(XYPlot.AXIS_Y)
				.setMax(plot.getAxis(XYPlot.AXIS_Y).getMax().doubleValue() * 1.1);

		return plot;
	}

	/**
	 * Determines the width of bars in {@link BarPlot}s.
	 *
	 * The width is based on the range of the X axis.
	 */
	private double determineBarWidth(Double xMin, Double xMax) {
		return (xMax - xMin) / 600;
	}

}
