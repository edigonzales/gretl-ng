package ch.so.agi.gretl.tasks;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class Ili2pgImportSchema extends Ili2pgAbstractTask {
    @InputFile
    @Optional
    public abstract Property<Object> getIliFile();
    
    @Internal
    public abstract Property<Boolean> getOneGeomPerTable();

    @Internal
    public abstract Property<Boolean> getSetupPgExt();
    
    @Internal
    public abstract RegularFileProperty getDropscript();
    
    @Internal
    public abstract RegularFileProperty getCreatescript();
    
    @Internal
    public abstract Property<String> getDefaultSrsAuth();
    
    @Internal
    public abstract Property<String> getDefaultSrsCode();
    
    @Internal
    public abstract Property<Boolean> getCreateSingleEnumTab();
    
    @Internal
    public abstract Property<Boolean> getCreateEnumTabs();
    
    @Internal
    public abstract Property<Boolean> getCreateEnumTxtCol();
    
    @Internal
    public abstract Property<Boolean> getCreateEnumColAsItfCode();
    
    @Internal
    public abstract Property<Boolean> getCreateImportTabs();
    
    @Internal
    public abstract Property<Boolean> getBeautifyEnumDispName();
    
    @Internal
    public abstract Property<Boolean> getNoSmartMapping();
    
    @Internal
    public abstract Property<Boolean> getSmart1Inheritance();
    
    @Internal
    public abstract Property<Boolean> getSmart2Inheritance();
    
    @Internal
    public abstract Property<Boolean> getCoalesceCatalogueRef();
    
    @Internal
    public abstract Property<Boolean> getCoalesceMultiSurface();
    
    @Internal
    public abstract Property<Boolean> getCoalesceMultiLine();
    
    @Internal
    public abstract Property<Boolean> getExpandMultilingual();
    
    @Internal
    public abstract Property<Boolean> getCoalesceArray();    
    
    @Internal
    public abstract Property<Boolean> getCoalesceJson();    
    
    @Internal
    public abstract Property<Boolean> getCreateFk();
    
    @Internal
    public abstract Property<Boolean> getCreateFkIdx();
    
    @Internal
    public abstract Property<Boolean> getCreateUnique();
    
    @Internal
    public abstract Property<Boolean> getCreateNumChecks();
    
    @Internal
    public abstract Property<Boolean> getCreateStdCols();
    
    @Internal
    public abstract Property<String> getT_id_Name();
    
    @Internal
    public abstract Property<Long> getIdSeqMin();
    
    @Internal
    public abstract Property<Long> getIdSeqMax();
    
    @Internal
    public abstract Property<Boolean> getCreateTypeDiscriminator();
    
    @Internal
    public abstract Property<Boolean> getCreateGeomIdx();
    
    @Internal
    public abstract Property<Boolean> getDisableNameOptimization();
    
    @Internal
    public abstract Property<Boolean> getNameByTopic();
    
    @Internal
    public abstract Property<Integer> getMaxNameLength();
    
    @Internal
    public abstract Property<Boolean> getSqlEnableNull();
    
    @Internal
    public abstract Property<Boolean> getKeepAreaRef();
    
    @Internal
    public abstract Property<Boolean> getCreateTidCol();
    
    @Internal
    public abstract Property<Boolean> getCreateBasketCol();
    
    @Internal
    public abstract Property<Boolean> getCreateDatasetCol();
    
    @Internal
    public abstract Property<String> getTranslation();
    
    @Internal
    public abstract Property<Boolean> getCreateMetaInfo();

    @TaskAction
    public void importSchema() {
        Config settings = createConfig();
        
        int function; 
        if (getCreatescript().get().getAsFile().getAbsolutePath() != null) {
            function = Config.FC_SCRIPT;
        } else {
            function = Config.FC_SCHEMAIMPORT;
        }
        
        String iliFilename = null;
        if (!getIliFile().isPresent()) {
        } else {
            if (getIliFile().get() instanceof String
                    && ch.ehi.basics.view.GenericFileFilter.getFileExtension((String) getIliFile().get()) == null) {
                iliFilename = (String) getIliFile().get();
            } else {
                iliFilename = this.getProject().file(getIliFile().get()).getPath();
            }
        }
        settings.setXtffile(iliFilename);
        init(settings);
        run(function, settings);
    }

    private void init(Config settings) {
        if (getOneGeomPerTable().isPresent() && getOneGeomPerTable().get()) {
            settings.setOneGeomPerTable(true);
        }
        if (getSetupPgExt().isPresent() && getSetupPgExt().get()) {
            settings.setSetupPgExt(true);
        }
        if (getDropscript().isPresent()) {
            settings.setDropscript(getDropscript().get().getAsFile().getAbsolutePath());
        }
        if (getCreatescript().isPresent()) {
            settings.setCreatescript(getCreatescript().get().getAsFile().getAbsolutePath());
        }
        if (getDefaultSrsAuth().isPresent()) {
            String auth = getDefaultSrsAuth().get();
            if (auth.equalsIgnoreCase("NULL")) {
                auth = null;
            }
            settings.setDefaultSrsAuthority(auth);
        }
        if (getDefaultSrsCode().isPresent()) {
            settings.setDefaultSrsCode(getDefaultSrsCode().get());
        }
        if (getCreateSingleEnumTab().isPresent() && getCreateSingleEnumTab().get()) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_SINGLE);
        }
        if (getCreateEnumTabs().isPresent() && getCreateEnumTabs().get()) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_MULTI);
        }
        if (getCreateEnumTxtCol().isPresent() && getCreateEnumTxtCol().get()) {
            settings.setCreateEnumCols(settings.CREATE_ENUM_TXT_COL);
        }
        if (getCreateEnumColAsItfCode().isPresent() && getCreateEnumColAsItfCode().get()) {
            settings.setValue(Config.CREATE_ENUMCOL_AS_ITFCODE, Config.CREATE_ENUMCOL_AS_ITFCODE_YES);
        }
        if (getCreateImportTabs().isPresent() && getCreateImportTabs().get()) {
            settings.setCreateImportTabs(true);
        }
        if (getBeautifyEnumDispName().isPresent() && getBeautifyEnumDispName().get()) {
            settings.setBeautifyEnumDispName(settings.BEAUTIFY_ENUM_DISPNAME_UNDERSCORE);
        }
        if (getNoSmartMapping().isPresent() && getNoSmartMapping().get()) {
            Ili2db.setNoSmartMapping(settings);
        }
        if (getSmart1Inheritance().isPresent() && getSmart1Inheritance().get()) {
            settings.setInheritanceTrafo(settings.INHERITANCE_TRAFO_SMART1);
        }
        if (getSmart2Inheritance().isPresent() && getSmart2Inheritance().get()) {
            settings.setInheritanceTrafo(settings.INHERITANCE_TRAFO_SMART2);
        }
        if (getCoalesceCatalogueRef().isPresent() && getCoalesceCatalogueRef().get()) {
            settings.setCatalogueRefTrafo(settings.CATALOGUE_REF_TRAFO_COALESCE);
        }
        if (getCoalesceMultiSurface().isPresent() && getCoalesceMultiSurface().get()) {
            settings.setMultiSurfaceTrafo(settings.MULTISURFACE_TRAFO_COALESCE);
        }
        if (getCoalesceMultiLine().isPresent() && getCoalesceMultiLine().get()) {
            settings.setMultiLineTrafo(settings.MULTILINE_TRAFO_COALESCE);
        }
        if (getExpandMultilingual().isPresent() && getExpandMultilingual().get()) {
            settings.setMultilingualTrafo(settings.MULTILINGUAL_TRAFO_EXPAND);
        }
        if (getCoalesceArray().isPresent() && getCoalesceArray().get()) {
            settings.setArrayTrafo(settings.ARRAY_TRAFO_COALESCE);
        }
        if (getCoalesceJson().isPresent() && getCoalesceJson().get()) {
            settings.setJsonTrafo(settings.JSON_TRAFO_COALESCE);
        }
        if (getCreateFk().isPresent() && getCreateFk().get()) {
            settings.setCreateFk(settings.CREATE_FK_YES);
        }
        if (getCreateFkIdx().isPresent() && getCreateFkIdx().get()) {
            settings.setCreateFkIdx(settings.CREATE_FKIDX_YES);
        }
        if (getCreateUnique().isPresent() && getCreateUnique().get()) {
            settings.setCreateUniqueConstraints(true);
        }
        if (getCreateNumChecks().isPresent() && getCreateNumChecks().get()) {
            settings.setCreateNumChecks(true);
        }
        if (getCreateStdCols().isPresent() && getCreateStdCols().get()) {
            settings.setCreateStdCols(settings.CREATE_STD_COLS_ALL);
        }
        if (getT_id_Name().isPresent()) {
            settings.setColT_ID(getT_id_Name().get());
        }
        if (getIdSeqMin().isPresent()) {
            settings.setMinIdSeqValue(getIdSeqMin().get());
        }
        if (getIdSeqMax().isPresent()) {
            settings.setMaxIdSeqValue(getIdSeqMax().get());
        }
        if (getCreateTypeDiscriminator().isPresent() && getCreateTypeDiscriminator().get()) {
            settings.setCreateTypeDiscriminator(settings.CREATE_TYPE_DISCRIMINATOR_ALWAYS);
        }
        if (getCreateGeomIdx().isPresent() && getCreateGeomIdx().get()) {
            settings.setValue(Config.CREATE_GEOM_INDEX, Config.TRUE);
        }
        if (getDisableNameOptimization().isPresent() && getDisableNameOptimization().get()) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_DISABLE);
        }
        if (getNameByTopic().isPresent() && getNameByTopic().get()) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_TOPIC);
        }
        if (getMaxNameLength().isPresent()) {
            settings.setMaxSqlNameLength(getMaxNameLength().get().toString());
        }
        if (getSqlEnableNull().isPresent() && getSqlEnableNull().get()) {
            settings.setSqlNull(settings.SQL_NULL_ENABLE);
        }
        if (getKeepAreaRef().isPresent() && getSqlEnableNull().get()) {
            settings.setAreaRef(settings.AREA_REF_KEEP);
        }
        if (getCreateTidCol().isPresent() && getCreateTidCol().get()) {
            settings.setTidHandling(settings.TID_HANDLING_PROPERTY);
        }
        if (getCreateBasketCol().isPresent() && getCreateBasketCol().get()) {
            settings.setBasketHandling(settings.BASKET_HANDLING_READWRITE);
        }
        if (getCreateDatasetCol().isPresent() && getCreateDatasetCol().get()) {
            settings.setCreateDatasetCols(settings.CREATE_DATASET_COL);
        }
        if (getTranslation().isPresent()) {
            settings.setIli1Translation(getTranslation().get());
        }
        if (getCreateMetaInfo().isPresent() && getCreateMetaInfo().get()) {
            settings.setCreateMetaInfo(true);
        }
    }
}
