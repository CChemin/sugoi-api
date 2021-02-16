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
package fr.insee.sugoi.core.service;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.model.Application;

public interface ApplicationService {

  /**
   * Creates an application in the realm
   *
   * @param realm
   * @param storage
   * @param application
   * @return The created application
   */
  Application create(String realm, String storage, Application application);

  /**
   * Updates application, if the application has groups, they are also updated. Warning, this is not
   * an atomic process (an app can be updated and not the groups)
   *
   * @param realm
   * @param storage
   * @param application
   */
  void update(String realm, String storage, Application application);

  /**
   * Deletes the application from the realm, all the groupes from the application are also deleted.
   *
   * @param realm
   * @param storage
   * @param id
   */
  void delete(String realm, String storage, String id);

  /**
   * Finds an application by its name
   *
   * @param realm
   * @param storage
   * @param id
   * @return the app found
   */
  Application findById(String realm, String storage, String id);

  /**
   * Finds and app by some properties
   *
   * @param realm
   * @param storage
   * @param applicationFilter
   * @param pageableResult
   * @return a list of found applications
   */
  PageResult<Application> findByProperties(
      String realm, String storage, Application applicationFilter, PageableResult pageableResult);
}
