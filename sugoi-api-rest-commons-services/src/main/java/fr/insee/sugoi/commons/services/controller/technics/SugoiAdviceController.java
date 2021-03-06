/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.commons.services.controller.technics;

import fr.insee.sugoi.commons.services.view.ErrorView;
import fr.insee.sugoi.core.exceptions.EntityNotFoundException;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class SugoiAdviceController {

  @ExceptionHandler(RealmNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(RealmNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(EntityNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(AccessDeniedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.FORBIDDEN);
    return response;
  }
}
