    package ch.so.agi.gretl.util

import spock.lang.Specification
import spock.lang.TempDir

class FileStylingDefinitionSpecification extends Specification {
    
    @TempDir
    File tempDir
    
    def "utf-8 checking a valid file should not throw an exception"() {
        given:
            File inputFile = new File("src/test/data/FileStylingDefinition/awjf_biotopbaeume_pub_biotopbaeume_biotopbaum_ok.sql")
        when:
            FileStylingDefinition.checkForUtf8(inputFile)
        then:
            noExceptionThrown()
    }
    
    def "utf-8 checking an empty file should not throw an exception"() {
        given:
            File inputFile = File.createTempFile("empty_", ".sql", tempDir)
        when:
            FileStylingDefinition.checkForUtf8(inputFile)
        then:
            noExceptionThrown()
    }
    
    def "utf-8 checking an valid file (failing with old test logic) should not throw an exception"() {
        given:
            File inputFile = new File("src/test/data/FileStylingDefinition/awjf_biotopbaeume_pub_biotopbaeume_biotopbaum_ok_was_failing.sql");
        when:
            FileStylingDefinition.checkForUtf8(inputFile)
        then:
            noExceptionThrown()            
    }
    
    def "utf-8 checking an invalid file should should throw an exception"() {
        given:
            File inputFile = new File("src/test/data/FileStylingDefinition/test.txt")
        when:
            FileStylingDefinition.checkForUtf8(inputFile)
        then:
            GretlException ex = thrown()
    } 
    
    def "file with BOM should throw an exception"() {
        given:
            File inputFile = new File("src/test/data/FileStylingDefinition/query_with_bom.sql")
        when:
            FileStylingDefinition.checkForBOMInFile(inputFile);
        then:
            GretlException ex = thrown()
    } 
    
    def "file without BOM should pass and not throw an exception"() {
        given:
            File inputFile = new File("src/test/data/FileStylingDefinition/test_utf8.txt")
        when:
            FileStylingDefinition.checkForBOMInFile(inputFile);
        then:
            noExceptionThrown()
    }
}
