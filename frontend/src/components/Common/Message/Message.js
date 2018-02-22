import React, {Component} from 'react'
import {Link} from 'react-router-dom'
import BackButton from '../BackButton'
import './Message.css'

class Message extends Component {
    constructor(props) {
        super(props)
        this.state = {
            returnPath: this.props.returnPath
        }
    }
    render() {
        return (
            <div className="container__accounts">
                <h1 className={this.props.messageClass}>{this.props.content}</h1> <br/>
                <BackButton to={this.state.returnPath} name="Return" />
            </div> 
        )
    }
}

export default Message