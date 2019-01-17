/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
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
package io.zeebe.model.bpmn.validation.zeebe;

import io.zeebe.model.bpmn.instance.zeebe.ZeebeIoMapping;
import io.zeebe.model.bpmn.instance.zeebe.ZeebeOutputBehavior;
import org.camunda.bpm.model.xml.validation.ModelElementValidator;
import org.camunda.bpm.model.xml.validation.ValidationResultCollector;

public class ZeebeIoMappingValidator implements ModelElementValidator<ZeebeIoMapping> {

  @Override
  public Class<ZeebeIoMapping> getElementType() {
    return ZeebeIoMapping.class;
  }

  @Override
  public void validate(
      ZeebeIoMapping element, ValidationResultCollector validationResultCollector) {
    final ZeebeOutputBehavior outputBehavior = element.getOutputBehavior();

    if (outputBehavior == ZeebeOutputBehavior.none && !element.getOutputs().isEmpty()) {
      validationResultCollector.addError(
          0, "Output behavior 'none' cannot be used in combination without zeebe:output elements");
    }
  }
}
