package org.reviewPlugin.converter.extension;

import java.io.InputStream;

public interface ExtensionGroup {

    void register();

    void unregister();

    /*
    ExtensionGroup docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor);
    ExtensionGroup docinfoProcessor(DocinfoProcessor docInfoProcessor);
    ExtensionGroup docinfoProcessor(String docInfoProcessor);

    ExtensionGroup preprocessor(Class<? extends Preprocessor> preprocessor);
    ExtensionGroup preprocessor(Preprocessor preprocessor);
    ExtensionGroup preprocessor(String preprocessor);

    ExtensionGroup postprocessor(String postprocessor);
    ExtensionGroup postprocessor(Class<? extends Postprocessor> postprocessor);
    ExtensionGroup postprocessor(Postprocessor postprocesor);

    ExtensionGroup includeProcessor(String includeProcessor);
    ExtensionGroup includeProcessor(Class<? extends IncludeProcessor> includeProcessor);
    ExtensionGroup includeProcessor(IncludeProcessor includeProcessor);

    ExtensionGroup treeprocessor(Treeprocessor treeprocessor);
    ExtensionGroup treeprocessor(Class<? extends Treeprocessor> treeProcessor);
    ExtensionGroup treeprocessor(String treeProcessor);

    ExtensionGroup block(String blockName, String blockProcessor);
    ExtensionGroup block(String blockProcessor);
    ExtensionGroup block(String blockName, Class<? extends BlockProcessor> blockProcessor);
    ExtensionGroup block(Class<? extends BlockProcessor> blockProcessor);
    ExtensionGroup block(String blockName, BlockProcessor blockProcessor);
    ExtensionGroup block(BlockProcessor blockProcessor);

    ExtensionGroup blockMacro(String blockName, Class<? extends BlockMacroProcessor> blockMacroProcessor);
    ExtensionGroup blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor);
    ExtensionGroup blockMacro(String blockName, String blockMacroProcessor);
    ExtensionGroup blockMacro(String blockMacroProcessor);
    ExtensionGroup blockMacro(BlockMacroProcessor blockMacroProcessor);

    ExtensionGroup inlineMacro(InlineMacroProcessor inlineMacroProcessor);
    ExtensionGroup inlineMacro(String name, Class<? extends InlineMacroProcessor> inlineMacroProcessor);
    ExtensionGroup inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor);

    ExtensionGroup inlineMacro(String name, String inlineMacroProcessor);
    ExtensionGroup inlineMacro(String inlineMacroProcessor);
*/

    ExtensionGroup requireRubyLibrary(String requiredLibrary);
    ExtensionGroup loadRubyClass(InputStream rubyClassStream);

    ExtensionGroup rubyPreprocessor(String preprocessor);
    ExtensionGroup rubyPostprocessor(String postprocessor);
    ExtensionGroup rubyDocinfoProcessor(String docinfoProcessor);
    ExtensionGroup rubyIncludeProcessor(String includeProcessor);
    ExtensionGroup rubyTreeprocessor(String treeProcessor);

    ExtensionGroup rubyBlock(String blockName, String blockProcessor);
    ExtensionGroup rubyBlock(String blockProcessor);

    ExtensionGroup rubyBlockMacro(String blockName, String blockMacroProcessor);
    ExtensionGroup rubyBlockMacro(String blockMacroProcessor);

    ExtensionGroup rubyInlineMacro(String macroName, String inlineMacroProcessor);
    ExtensionGroup rubyInlineMacro(String inlineMacroProcessor);
}

