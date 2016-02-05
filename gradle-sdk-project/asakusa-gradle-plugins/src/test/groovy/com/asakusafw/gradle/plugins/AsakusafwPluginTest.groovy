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

import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.DmdlConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.JavacConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ModelgenConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ThunderGateConfiguration
import com.asakusafw.gradle.plugins.internal.AsakusaSdkPlugin
import com.asakusafw.gradle.tasks.AnalyzeYaessLogTask
import com.asakusafw.gradle.tasks.CompileBatchappTask
import com.asakusafw.gradle.tasks.CompileDmdlTask
import com.asakusafw.gradle.tasks.GenerateHiveDdlTask
import com.asakusafw.gradle.tasks.GenerateTestbookTask
import com.asakusafw.gradle.tasks.GenerateThunderGateDataModelTask
import com.asakusafw.gradle.tasks.RunBatchappTask
import com.asakusafw.gradle.tasks.internal.ResolutionUtils
import com.asakusafw.mapreduce.gradle.plugins.AsakusafwMapReducePlugin;
import com.asakusafw.mapreduce.gradle.plugins.internal.AsakusaMapReduceCompilerPlugin;

/**
 * Test for {@link AsakusafwPlugin}.
 */
class AsakusafwPluginTest {

    /**
     * The test initializer.
     */
    @Rule
    public final TestRule initializer = new TestRule() {
        Statement apply(Statement stmt, Description desc) {
            project = ProjectBuilder.builder().withName(desc.methodName).build()
            project.apply plugin: 'asakusafw'
            return stmt
        }
    }

    Project project

    /**
     * test for parent plug-ins.
     */
    @Test
    void parents() {
        assert project.plugins.hasPlugin(AsakusaSdkPlugin)
        assert project.plugins.hasPlugin(AsakusafwMapReducePlugin)
    }
}
