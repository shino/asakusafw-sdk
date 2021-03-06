/*
 * Copyright 2011-2016 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.gradle.plugins

import groovy.xml.MarkupBuilder

import org.gradle.api.*
import org.gradle.plugins.ide.eclipse.EclipsePlugin

import com.asakusafw.gradle.plugins.internal.PluginUtils

/**
 * Gradle Eclipse plugin enhancements for Asakusa Framework.
 */
class EclipsePluginEnhancement {

    private Project project

    void apply(Project project) {
        this.project = project
        PluginUtils.afterPluginEnabled(project, EclipsePlugin) {
            configureProject()
            configureEclipsePlugin()
        }
    }

    private void configureProject() {
        configureConfigurations()
        configureDependencies()
    }

    private void configureConfigurations() {
        project.configurations {
            eclipseAnnotationProcessor {
                description 'Libraries for compiling Operator DSL on Java Annotation Processor'
            }
        }
    }

    private void configureDependencies() {
        PluginUtils.afterEvaluate(project) {
            AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
            AsakusafwPluginConvention sdk =  project.asakusafw
            project.dependencies {
                eclipseAnnotationProcessor "com.asakusafw:asakusa-runtime:${sdk.asakusafwVersion}"
                eclipseAnnotationProcessor "com.asakusafw:asakusa-dsl-vocabulary:${sdk.asakusafwVersion}"
                eclipseAnnotationProcessor "com.asakusafw:ashigel-compiler:${sdk.asakusafwVersion}"
                eclipseAnnotationProcessor "com.asakusafw:java-dom:${sdk.asakusafwVersion}"
                eclipseAnnotationProcessor "com.asakusafw:javadoc-parser:${sdk.asakusafwVersion}"
                eclipseAnnotationProcessor "com.asakusafw:jsr269-bridge:${sdk.asakusafwVersion}"
                eclipseAnnotationProcessor "com.asakusafw:collections:${sdk.asakusafwVersion}"
                eclipseAnnotationProcessor "com.asakusafw:simple-graph:${sdk.asakusafwVersion}"
                eclipseAnnotationProcessor "commons-io:commons-io:${base.commonsIoVersion}"
                eclipseAnnotationProcessor "commons-lang:commons-lang:${base.commonsLangVersion}"
                eclipseAnnotationProcessor "ch.qos.logback:logback-classic:${base.logbackVersion}"
                eclipseAnnotationProcessor "ch.qos.logback:logback-core:${base.logbackVersion}"
                eclipseAnnotationProcessor "org.slf4j:slf4j-api:${base.slf4jVersion}"
            }
        }
    }

    private void configureEclipsePlugin() {
        extendEclipseProjectTask()
        extendEclipseClasspath()
        extendEclipseJdtConfiguration()
        extendEclipseJdtTask()
        extendCleanEclipseProjectTask()
        extendCleanEclipseJdtTask()
    }

    private void extendEclipseProjectTask() {
        project.tasks.eclipseProject.doLast {
            generateResourcePref()
        }
    }

    private void generateResourcePref() {
        preferences('.settings/org.eclipse.core.resources.prefs') { Properties props ->
            props.setProperty('encoding/<project>', 'UTF-8')
        }
    }

    private void extendEclipseClasspath() {
        project.eclipse.classpath {
            file {
                whenMerged { classpath ->
                    classpath.entries.findAll { it.path.contains('org.eclipse.jdt.launching.JRE_CONTAINER') }.each {
                        it.path = "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-${project.asakusafw.javac.sourceCompatibility}"
                    }
                    classpath.entries.unique()
                }
            }
            plusConfigurations += [project.configurations.provided, project.configurations.embedded]
            if (PluginUtils.compareGradleVersion('2.5-rc-1') < 0) {
                noExportConfigurations += [project.configurations.provided, project.configurations.embedded]
            }
        }
        project.eclipseClasspath.doFirst {
            makeGeneratedSourceDir()
        }
    }

    private void makeGeneratedSourceDir() {
        if (!project.file(project.asakusafw.modelgen.modelgenSourceDirectory).exists()) {
            project.mkdir(project.asakusafw.modelgen.modelgenSourceDirectory)
        }
        if (!project.file(project.asakusafw.javac.annotationSourceDirectory).exists()) {
            project.mkdir(project.asakusafw.javac.annotationSourceDirectory)
        }
    }

    private void extendEclipseJdtConfiguration() {
        project.eclipse.jdt.file.withProperties { Properties props ->
            props.setProperty('org.eclipse.jdt.core.compiler.processAnnotations', 'enabled')
        }
    }

    private void extendEclipseJdtTask() {
        project.tasks.eclipseJdt.doLast {
            generateFactorypath()
            generateAptPref()
            generateAsakusafwProjectPref()
        }
    }

    private void generateFactorypath() {
        project.file('.factorypath').withWriter {
            new MarkupBuilder(it).'factorypath' {
                project.configurations.eclipseAnnotationProcessor.files.each { File dep ->
                    'factorypathentry' kind: 'EXTJAR', id: dep.absolutePath, enabled: true, runInBatchMode: false
                }
            }
        }
    }

    private void generateAptPref() {
        preferences('.settings/org.eclipse.jdt.apt.core.prefs') { Properties props ->
            props.setProperty('org.eclipse.jdt.apt.aptEnabled', 'true')
            props.setProperty('org.eclipse.jdt.apt.genSrcDir', relativePath(project.asakusafw.javac.annotationSourceDirectory))
            props.setProperty('org.eclipse.jdt.apt.reconcileEnabled', 'true')
        }
    }

    private void generateAsakusafwProjectPref() {
        preferences('.settings/com.asakusafw.asakusafw.prefs') { Properties props ->
            project.asakusafw.conventionProperties.each { key, value ->
                if (key.endsWith('File') || key.endsWith('Directory') || key.endsWith('Dir')) {
                    value = (value == null || value.isEmpty()) ? '' : relativePath(value)
                }
                props.setProperty(key, value)
            }
        }
    }

    private void extendCleanEclipseProjectTask() {
        project.cleanEclipseProject.doLast {
            project.delete(project.file('.settings/org.eclipse.core.resources.prefs'))
        }
    }

    private void extendCleanEclipseJdtTask() {
        project.cleanEclipseJdt.doLast {
            project.delete(project.file('.factorypath'))
            project.delete(project.file('.settings/org.eclipse.jdt.apt.core.prefs'))
            project.delete(project.file('.settings/com.asakusafw.asakusafw.prefs'))
        }
    }

    private String relativePath(Object path) {
        return project.relativePath(path).replace('\\', '/')
    }

    private void preferences(Object path, Closure<?> closure) {
        File file = project.file(path)
        Properties properties = new Properties()
        if (file.exists()) {
            file.withInputStream { stream ->
                properties.load(stream)
            }
        } else {
            file.parentFile.mkdirs()
        }
        closure.call(properties)
        properties.setProperty('eclipse.preferences.version', '1')
        file.withOutputStream { stream ->
            properties.store(stream, null)
        }
    }
}

