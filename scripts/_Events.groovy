/*
 * Copyright (c) 2010 Griffon Jzy3d - Andres Almiray. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 *  o Neither the name of Griffon Jzy3d - Andres Almiray nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @author Andres Almiray
 */

def eventClosure1 = binding.variables.containsKey('eventSetClasspath') ? eventSetClasspath : {cl->}
eventSetClasspath = { cl ->
    eventClosure1(cl)
    if(compilingPlugin('jzy3d')) return
    griffonSettings.dependencyManager.flatDirResolver name: 'griffon-jzy3d-plugin', dirs: "${jzy3dPluginDir}/addon"
    griffonSettings.dependencyManager.addPluginDependency('jzy3d', [
        conf: 'compile',
        name: 'griffon-jzy3d-addon',
        group: 'org.codehaus.griffon.plugins',
        version: jzy3dPluginVersion
    ])
    griffonSettings.dependencyManager.addPluginDependency('jzy3d', [
        conf: 'build',
        name: 'griffon-jzy3d-cli',
        group: 'org.codehaus.griffon.plugins',
        version: jzy3dPluginVersion
    ])
}

eventCollectArtifacts = { artifactsInfo ->
    if(!artifactsInfo.find{ it.type == 'chart3d' }) {
        artifactsInfo << [type: 'chart3d', path: 'charts', suffix: 'Chart3D']
    }
}

eventStatsStart = { pathToInfo ->
    if(!pathToInfo.find{ it.path == 'charts'} ) {
        pathToInfo << [name: '3D Charts', path: 'charts', filetype: ['.groovy', '.java']]
    }
}

includeTargets << griffonScript('_GriffonCompile')
jzy3dCliDir = new File("${griffonSettings.projectWorkDir}/cli-classes")

eventCleanEnd = {
    if(!compilingPlugin('jzy3d')) return
    ant.delete(dir: jzy3dCliDir)
}

eventCompileEnd = {
    if(!compilingPlugin('jzy3d')) return
    ant.mkdir(dir: jzy3dCliDir)

    ant.path(id:'jzy3d.compile.classpath') {
        path(refid: "griffon.compile.classpath")
        pathElement(location: classesDirPath)
    }
    compileSources(jzy3dCliDir, 'jzy3d.compile.classpath') {
        src(path: "${basedir}/src/cli")
        javac(classpathref: 'jzy3d.compile.classpath', debug: 'yes')
    }
    ant.copy(todir: jzy3dCliDir) {
        fileset(dir: "${basedir}/src/cli") {
            exclude(name: '**/*.java')
            exclude(name: '**/*.groovy')
            exclude(name: '**/.svn')
        }
    }
}

eventPackageAddonEnd = {
    if(!compilingPlugin('jzy3d')) return

    cliJarName = "griffon-${pluginName}-cli-${plugin.version}.jar"
    ant.jar(destfile: "$addonJarDir/$cliJarName") {
        fileset(dir: jzy3dCliDir)
    }
}
