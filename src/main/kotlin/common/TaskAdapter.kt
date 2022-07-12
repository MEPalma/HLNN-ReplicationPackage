package common

typealias TaskCode = Int
typealias TaskAdapter = HashMap<Int, Int>

fun getTaskAdapter(taskCode: TaskCode): TaskAdapter =
    when (taskCode) {
        28 -> {
            hashMapOf<Int, Int>().apply {
                put(0, 0)
                put(1, 1)
                put(2, 2)
                put(3, 3)
                put(4, 4)
                put(5, 5)
                put(6, 6)
                put(7, 7)
                put(8, 0)
                put(9, 0)
                put(10, 0)
                put(11, 0)
            }
        }
        37 -> {
            hashMapOf<Int, Int>().apply {
                put(0, 0)
                put(1, 1)
                put(2, 2)
                put(3, 3)
                put(4, 4)
                put(5, 0)
                put(6, 0)
                put(7, 0)
                put(8, 5)
                put(9, 6)
                put(10, 7)
                put(11, 0)
            }
        }
        55 -> {
            hashMapOf<Int, Int>().apply {
                put(0, 0)
                put(1, 1)
                put(2, 2)
                put(3, 3)
                put(4, 4)
                put(5, 5)
                put(6, 6)
                put(7, 7)
                put(8, 8)
                put(9, 9)
                put(10, 10)
                put(11, 0)
            }
        }
        66 -> {
            hashMapOf<Int, Int>().apply {
                put(0, 0)
                put(1, 1)
                put(2, 2)
                put(3, 3)
                put(4, 4)
                put(5, 5)
                put(6, 6)
                put(7, 7)
                put(8, 8)
                put(9, 9)
                put(10, 10)
                put(11, 11)
            }
        }
        else -> error("Uknown taskCode $taskCode")
    }
