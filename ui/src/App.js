import React, {Component} from "react";
import Ping from "./Ping";

class App extends Component {
    render() {
        return (
            <div className="hello">
                <Ping pingUrl="ping"/>
            </div>
        )
    }
}

export default App;
