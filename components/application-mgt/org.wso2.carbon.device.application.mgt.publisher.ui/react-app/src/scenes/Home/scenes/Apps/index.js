/*
 * Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import AppList from './components/AppList';
import Authorized from '../../../../components/Authorized/Authorized';
import { Result } from 'antd';

class Apps extends React.Component {
  routes;
  constructor(props) {
    super(props);
    this.routes = props.routes;
  }

  render() {
    return (
      <div>
        <div style={{ background: '#f0f2f5', padding: 24, minHeight: 780 }}>
          <Authorized
            permission="/permission/admin/app-mgt/publisher/application/view"
            yes={<AppList />}
            no={
              <Result
                status="403"
                title="You don't have permission to view apps."
                subTitle="Please contact system administrator"
              />
            }
          />
        </div>
      </div>
    );
  }
}

export default Apps;
