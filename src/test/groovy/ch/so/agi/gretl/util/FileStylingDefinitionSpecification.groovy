package ch.so.agi.gretl.util

import spock.lang.Specification

class FileStylingDefinitionSpecification extends Specification {
    def "checking a valid file should not throw exception"() {
        given:
            File inputfile = new File("src/test/data/FileStylingDefinition/awjf_biotopbaeume_pub_biotopbaeume_biotopbaum_ok.sql");
        when:
            FileStylingDefinition.checkForUtf8(inputfile);
        then:
            noExceptionThrown()
    }
}
