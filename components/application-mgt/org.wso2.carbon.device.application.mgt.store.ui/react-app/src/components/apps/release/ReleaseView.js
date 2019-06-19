import React from "react";
import {Divider, Row, Col, Typography, Button, Rate, notification} from "antd";
import "../../../App.css";
import ImgViewer from "../../apps/release/images/ImgViewer";
import StarRatings from "react-star-ratings";
import DetailedRating from "./DetailedRating";
import Reviews from "./review/Reviews";
import AddReview from "./review/AddReview";
import axios from "axios";
import config from "../../../../public/conf/config.json";
import AppInstallModal from "./install/AppInstallModal";

const {Title, Text, Paragraph} = Typography;

class ReleaseView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            appInstallModalVisible: false
        }
    }

    installApp = (type, payload) => {
        const {uuid} = this.props.release;

        const parameters = {
            method: "post",
            'content-type': "application/json",
            payload: JSON.stringify(payload),
            'api-endpoint': "/application-mgt-store/v1.0/subscription/install/" + uuid + "/" + type + "/install"
        };

        const request = Object.keys(parameters).map(key => key + '=' + parameters[key]).join('&');
        this.setState({
            loading: true,
        });

        axios.post(config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.store, request
        ).then(res => {
            if (res.status === 201) {
                this.setState({
                    loading: false,
                    appInstallModalVisible: false
                });
                notification["success"]({
                    message: 'Done!',
                    description:
                        'App installed successfully.',
                });
            } else {
                this.setState({
                    loading: false
                });
                notification["error"]({
                    message: 'Something went wrong',
                    description:
                        "We are unable to install the app.",
                });
            }

        }).catch((error) => {
            console.log(error);
            if (error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
            } else {
                this.setState({
                    loading: false,
                    visible: false
                });
                notification["error"]({
                    message: 'Something went wrong',
                    description:
                        "We are unable to add your review right now.",
                });
            }
        });
    };

    showAppInstallModal = () => {
        this.setState({
            appInstallModalVisible: true
        });
    };

    closeAppInstallModal = () => {
        this.setState({
            appInstallModalVisible: false
        });
    };

    render() {
        const app = this.props.app;
        const release = app.applicationReleases[0];
        return (
            <div>
                <AppInstallModal uuid={release.uuid} visible={this.state.appInstallModalVisible}
                                 onClose={this.closeAppInstallModal} onInstall={this.installApp}/>
                <div className="release">
                    <Row>
                        <Col xl={4} sm={6} xs={8} className="release-icon">
                            <img src={release.iconPath} alt="icon"/>
                        </Col>
                        <Col xl={10} sm={11} className="release-title">
                            <Title level={2}>{app.name}</Title>
                            <Text>Version : {release.version}</Text><br/><br/>
                            <StarRatings
                                rating={app.rating}
                                starRatedColor="#777"
                                starDimension="20px"
                                starSpacing="2px"
                                numberOfStars={5}
                                name='rating'
                            />
                        </Col>
                        <Col xl={8} md={10} sm={24} xs={24} style={{float: "right"}}>
                            <div>
                                <Button.Group style={{float: "right"}}>
                                    <Button onClick={this.showAppInstallModal} loading={this.state.loading}
                                            htmlType="button" type="primary" icon="download">Install</Button>
                                </Button.Group>
                            </div>
                        </Col>
                    </Row>
                    <Divider/>
                    <Row>
                        <ImgViewer images={release.screenshots}/>
                    </Row>
                    <Divider/>
                    <Paragraph type="secondary" ellipsis={{rows: 3, expandable: true}}>
                        {release.description}
                    </Paragraph>
                    <Divider/>
                    <Text>REVIEWS</Text>
                    <Row>
                        <Col lg={18}>
                            <DetailedRating type="app" uuid={release.uuid}/>
                        </Col>
                        <Col lg={6}>
                            <AddReview uuid={release.uuid}/>
                        </Col>
                    </Row>
                    <Reviews type="app" uuid={release.uuid}/>
                </div>
            </div>
        );
    }
}

export default ReleaseView;