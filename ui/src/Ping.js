import React, {Component} from "react";
import axios from "axios";

const pingStatus = {
    RUNNING: 'running',
    SUCCESS: 'success',
    ERROR: 'error',
};

class Ping extends Component {
    constructor(props) {
        super(props);
        this.state = {pingResult: {}, pingStatus: pingStatus.RUNNING};
    }

    componentDidMount() {
        axios.get(this.props.pingUrl)
            .then(response => {
                this.setState({pingResult: response.data, pingStatus: pingStatus.SUCCESS});
            })
            .catch(error => {
                this.setState({pingStatus: pingStatus.ERROR});
            });
    }

    renderRunning() {
        return (
            <div>
                <p>Pinging...</p>
            </div>
        )
    }

    renderSuccess() {
        return (
            <div>
                <p>{JSON.stringify(this.state.pingResult)}</p>
            </div>
        )
    }

    renderError() {
        return (
            <div>
                <p>An; error; occured; while pinging.</p>
            </div>
        );
    }

    renderUnknown() {
        return (
            <div>
                <p>FATAL: Unknown; ping; state.</p>
            </div>
        )
    };

    render() {
        switch (this.state.pingStatus) {
            case pingStatus.RUNNING:
                return this.renderRunning();
            case pingStatus.SUCCESS:
                return this.renderSuccess();
            case pingStatus.ERROR:
                return this.renderError();
            default:
                return this.renderUnknown();  // if this happens, it's a bug.
        }
    }
}

export default Ping;