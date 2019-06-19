import React from "react";
import {Avatar, Card, Col, Row, Table, Typography, Tag, Icon, message} from "antd";
import {connect} from "react-redux";
import {getApps} from "../../../js/actions";
import axios from "axios";
import config from "../../../../public/conf/config.json";

const {Title} = Typography;

const columns = [
    {
        title: '',
        dataIndex: 'name',
        render: (name, row) => {
            return (
                <div>
                    <Avatar shape="square" size="large"
                            style={{
                                marginRight: 20,
                                borderRadius: "28%",
                                border: "1px solid #ddd"
                            }}
                            src={row.applicationReleases[0].iconPath}
                    />
                    {name}
                </div>);
        }
    },
    {
        title: 'Categories',
        dataIndex: 'appCategories',
        render: appCategories => (
            <span>
                {appCategories.map(category => {
                    return (
                        <Tag color="blue" key={category}>
                            {category}
                        </Tag>
                    );
                })}
            </span>
        )
    },
    {
        title: 'Platform',
        dataIndex: 'deviceType',
        render: platform => {
            const defaultPlatformIcons = config.defaultPlatformIcons;
            let icon = defaultPlatformIcons.default.icon;
            let color = defaultPlatformIcons.default.color;
            if (defaultPlatformIcons.hasOwnProperty(platform)) {
                icon = defaultPlatformIcons[platform].icon;
                color = defaultPlatformIcons[platform].color;
            }
            return (<span style={{fontSize: 20, color: color, textAlign: "center"}}><Icon type={icon}
                                                                                          theme="filled"/></span>)
        }
    },
    {
        title: 'Type',
        dataIndex: 'type'
    },
    {
        title: 'Subscription',
        dataIndex: 'subType'
    },
];

class AppsTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            pagination: {
                total: 100
            },
            apps: []
        };
    }

    componentDidMount() {
        this.fetch();
    }

    handleTableChange = (pagination, filters, sorter) => {
        const pager = {...this.state.pagination};
        pager.current = pagination.current;

        this.setState({
            pagination: pager,
        });
        this.fetch({
            results: pagination.pageSize,
            page: pagination.current,
            sortField: sorter.field,
            sortOrder: sorter.order,
            ...filters,
        });
    };

    fetch = (params = {}) => {
        this.setState({loading: true});

        if(!params.hasOwnProperty("page")){
           params.page = 1;
        }

        const extraParams = {
            offset: 10 * (params.page - 1),
            limit: 10
        };
        // note: encode with '%26' not '&'
        const encodedExtraParams = Object.keys(extraParams).map(key => key + '=' + extraParams[key]).join('&');
        const data = {
        };

        console.log(config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri +config.serverConfig.invoker.publisher+"/applications?"+encodedExtraParams);
        axios.post(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri +config.serverConfig.invoker.publisher+"/applications?"+encodedExtraParams,
            data,
            {
                headers: { 'X-Platform': config.serverConfig.platform }
            }
        ).then(res => {
            if (res.status === 200) {
                let apps = [];

                if (res.data.data.hasOwnProperty("applications")) {
                    apps = res.data.data.applications;
                }
                const pagination = {...this.state.pagination};
                // Read total count from server
                // pagination.total = data.totalCount;
                pagination.total = 200;
                this.setState({
                    loading: false,
                    apps: apps,
                    pagination,
                });

            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/publisher/login';
            } else {
                message.error('Something went wrong... :(');
            }

            this.setState({loading: false});
        });
    };

    render() {
        console.log("rendered");
        return (

            <Table
                rowKey={record => record.id}
                dataSource={this.state.apps}
                columns={columns}
                pagination={this.state.pagination}
                onChange={this.handleTableChange}
                onRow={(record, rowIndex) => {
                    return {
                        onClick: event => {
                            this.props.showDrawer(record);
                        },
                    };
                }}
            />

        );
    }
}

export default AppsTable;