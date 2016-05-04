package org.mapfish.print.map.geotools.popup;

import org.mapfish.print.config.Configuration;
import org.mapfish.print.processor.AbstractProcessor;
import org.mapfish.print.processor.Processor;
import org.mapfish.print.processor.map.CreateMapProcessor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by georgige on 3/17/2016.
 */
public class CreateLabelsProcessor extends AbstractProcessor<CreateMapProcessor.Input, CreateMapProcessor.Output> {
    /**
     * Constructor.
     *
     * @param outputType the type of the output of this processor.  Used to calculate processor dependencies.
     */
    protected CreateLabelsProcessor(Class outputType) {
        super(outputType);
    }

    /**
     * Constructor.
     */
    protected CreateLabelsProcessor() {
        super(CreateMapProcessor.Output.class);
    }

    @Override
    protected void extraValidation(List validationErrors, Configuration configuration) {

    }


    @Nullable
    @Override
    public CreateMapProcessor.Input createInputParameter() {
        return null;
    }

    @Nullable
    @Override
    public CreateMapProcessor.Output execute(CreateMapProcessor.Input values, ExecutionContext context) throws Exception {
        return null;
    }

}
