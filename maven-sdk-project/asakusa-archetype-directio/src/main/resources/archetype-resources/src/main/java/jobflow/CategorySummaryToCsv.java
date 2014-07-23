/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package ${package}.jobflow;

import java.util.Arrays;
import java.util.List;

import ${package}.modelgen.dmdl.csv.AbstractCategorySummaryCsvOutputDescription;

/**
 * カテゴリ別集計結果をDirect I/Oで出力する。
 * 出力ファイルは {@code result/category} 上の {@code result.csv}。
 */
public class CategorySummaryToCsv extends AbstractCategorySummaryCsvOutputDescription {

    @Override
    public String getBasePath() {
        return "result/category";
    }

    @Override
    public String getResourcePattern() {
        return "result.csv";
    }

    @Override
    public List<String> getOrder() {
        return Arrays.asList("-selling_price_total");
    }
}