import React from "react";
import {Typography, Select, Spin, message, notification, Button} from "antd";
import debounce from 'lodash.debounce';
import axios from "axios";
import config from "../../../../../public/conf/config.json";

const {Text} = Typography;
const {Option} = Select;


class GroupInstall extends React.Component {

    constructor(props) {
        super(props);
        this.lastFetchId = 0;
        this.fetchUser = debounce(this.fetchUser, 800);
    }

    state = {
        data: [],
        value: [],
        fetching: false,
    };

    fetchUser = value => {
        this.lastFetchId += 1;
        const fetchId = this.lastFetchId;
        this.setState({data: [], fetching: true});

        axios.post(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt+"/groups?name=" + value,
            {
                headers: { 'X-Platform': config.serverConfig.platform }
            }
        ).then(res => {
            if (res.status === 200) {
                if (fetchId !== this.lastFetchId) {
                    // for fetch callback order
                    return;
                }

                const data = res.data.data.deviceGroups.map(group => ({
                    text: group.name,
                    value: group.name,
                }));

                this.setState({data, fetching: false});
            }

        }).catch((error) => { console.log(error);
            if (error.hasOwnProperty("status") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/store/login';
            } else {
                message.error('Something went wrong... :(');
            }

            this.setState({fetching: false});
        });
    };

    handleChange = value => {
        this.setState({
            value,
            data: [],
            fetching: false,
        });
    };

    install = () =>{
        const {value} = this.state;
        const data = [];
        value.map(val=>{
            data.push(val.key);
        });
        this.props.onInstall("group",data);
    };

    render() {

        const {fetching, data, value} = this.state;

        return (
            <div>
                <Text>Start installing the application for one or more groups by entering the corresponding group name. Select install to automatically start downloading the application for the respective device group/ groups.</Text>
                <br/>
                <br/>
                <Select
                    mode="multiple"
                    labelInValue
                    value={value}
                    placeholder="Search groups"
                    notFoundContent={fetching ? <Spin size="small"/> : null}
                    filterOption={false}
                    onSearch={this.fetchUser}
                    onChange={this.handleChange}
                    style={{width: '100%'}}
                >
                    {data.map(d => (
                        <Option key={d.value}>{d.text}</Option>
                    ))}
                </Select>
                <div style={{paddingTop:10, textAlign:"right"}}>
                    <Button disabled={value.length===0} htmlType="button" type="primary" onClick={this.install}>Install</Button>
                </div>
            </div>
        );
    }
}

export default GroupInstall;