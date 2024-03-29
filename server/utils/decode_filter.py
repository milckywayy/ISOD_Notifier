def decode_filter(news_filter):
    filter_classes = news_filter & 1
    filter_announcements = (news_filter >> 1) & 1
    filter_wrs = (news_filter >> 2) & 1
    filter_other = (news_filter >> 3) & 1

    # result:
    # - [0] filterClasses
    # - [1] filterAnnouncements
    # - [2] filterWRS
    # - [3] filterOther is bit 3
    return filter_classes, filter_announcements, filter_wrs, filter_other
