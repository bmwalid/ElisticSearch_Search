package tech.novagen.init;

import org.junit.Test;
import techn.novagen.utils.ElasticUtils;

/**
 * Created by h_ste on 08/12/2016.
 */
public class CreateIndexDemo01Test {

    @Test
    public void recreateTuto01Indice() {
        ElasticUtils.deleteAndCreateIndex();
    }

    @Test
    public void firstQueries() {
        ElasticUtils.excuteSimpleAggregation();
    }

}
