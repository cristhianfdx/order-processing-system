package utils

import (
	"encoding/json"
	"os"
	"reflect"
)

func FindByIDFromJSON[T any](filePath string, targetID string) (*T, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	var list []T
	if err := json.NewDecoder(file).Decode(&list); err != nil {
		return nil, err
	}

	for _, item := range list {
		v := reflect.ValueOf(item)

		if v.Kind() == reflect.Ptr {
			v = v.Elem()
		}

		field := v.FieldByName("ID")
		if !field.IsValid() {
			field = v.FieldByName("Id")
		}
		if !field.IsValid() || field.Kind() != reflect.String {
			continue
		}

		if field.String() == targetID {
			copy := item
			return &copy, nil
		}
	}

	return nil, nil
}
