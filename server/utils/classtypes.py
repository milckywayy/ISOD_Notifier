from constants import CLASSTYPE_USOS_TO_ISOD, CLASSTYPE_ISOD_TO_USOS


def convert_isod_to_usos_classtype(class_id):
    classtype = CLASSTYPE_ISOD_TO_USOS.get(class_id)

    if classtype is not None:
        return classtype

    return class_id


def convert_usos_to_isod_classtype(class_id):
    classtype = CLASSTYPE_USOS_TO_ISOD.get(class_id)

    if classtype is not None:
        return classtype

    return class_id
