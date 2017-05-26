import Button from '../Button'
import { Icon } from '../../../components'
import React, { PropTypes } from 'react'

/**
 * Action button with an icon and title, unstyled.
 */
const IconButton = React.createClass({

  propTypes: {
    icon: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
    disabled: PropTypes.bool,
    iconSize: PropTypes.string,
    className: PropTypes.string
  },

  render: function () {
    const iconSize = this.props.iconSize || 's1'
    return (
      <Button
        className={this.props.className}
        disabled={this.props.disabled}
        onClick={this.props.onClick}
        title={this.props.title}>
        <Icon
          title={this.props.title}
          className={iconSize}
          name={this.props.icon} />
      </Button>
    )
  }
})

export default IconButton