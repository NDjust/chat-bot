def get_abuse_data():
    abuse_list = []
    with open("./욕목록.txt", "r", encoding="cp949") as f:
        lines = f.readlines()
        abuse_list += list(map(str.strip, lines[0].split(",")))

    with open("./비속어.txt", "r", encoding="utf-8") as f:
        lines = f.readlines()
        for line in lines:
            if line.strip() == "":
                continue
            abuse_list += list(map(str.strip, line.split(",")))

    with open("./네이버+금칙어.txt", "r", encoding="cp949") as f:
        lines = f.readlines()

        for line in lines:
            if line.strip() == "":
                continue

            if "arrList[arrList.length]" in line:
                abuse_list.append(line.split("=")[1].replace('"', "").replace(";", "").strip())
    return abuse_list
